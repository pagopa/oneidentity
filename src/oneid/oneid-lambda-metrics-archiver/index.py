#!/usr/bin/env python3
"""Purpose: Export aggregated CloudWatch metrics to S3 from an AWS Lambda function.

Usage examples:
  Invoke the Lambda with its configured environment variables.
  Optionally override `months_back`, `reference_time`, `s3_bucket`, or `s3_prefix` in the event payload.
"""

from __future__ import annotations

import calendar
import json
import logging
import os
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any
from urllib.parse import quote

import boto3  # pyright: ignore[reportMissingImports]


LOGGER = logging.getLogger()
LOGGER.setLevel(logging.INFO)


@dataclass(frozen=True)
class DimensionSource:
    table_name: str
    attribute_name: str


@dataclass(frozen=True)
class MetricDefinition:
    metric_name: str
    dimension_name: str


@dataclass(frozen=True)
class Settings:
    aws_region: str
    cloudwatch_namespace: str
    export_env: str
    s3_bucket: str
    s3_prefix: str
    months_back: int
    period_seconds: int
    dimension_sources: dict[str, DimensionSource]
    metric_definitions: list[MetricDefinition]


def subtract_months(value: datetime, months: int) -> datetime:
    total_month_index = (value.year * 12 + value.month - 1) - months
    target_year = total_month_index // 12
    target_month = total_month_index % 12 + 1
    target_day = min(value.day, calendar.monthrange(target_year, target_month)[1])
    return value.replace(year=target_year, month=target_month, day=target_day)


def parse_reference_time(reference_time: str | None) -> datetime | None:
    if not reference_time:
        return None

    normalized_reference_time = reference_time.replace("Z", "+00:00")
    parsed_reference_time = datetime.fromisoformat(normalized_reference_time)
    if parsed_reference_time.tzinfo is None:
        return parsed_reference_time.replace(tzinfo=timezone.utc)

    return parsed_reference_time.astimezone(timezone.utc)


def resolve_time_range(months_back: int, reference_time: str | None) -> tuple[datetime, datetime]:
    end_time = parse_reference_time(reference_time) or datetime.now(timezone.utc)
    start_time = subtract_months(end_time, months_back)
    return start_time, end_time


def load_json_config(raw_value: Any, environment_name: str) -> Any:
    resolved_value = raw_value if raw_value is not None else os.environ[environment_name]
    if isinstance(resolved_value, (dict, list)):
        return resolved_value

    return json.loads(resolved_value)


def load_settings(event: dict[str, Any]) -> Settings:
    log_level = str(event.get("log_level") or os.environ.get("LOG_LEVEL", "INFO")).upper()
    LOGGER.setLevel(log_level)

    dimension_sources_payload = load_json_config(event.get("dimension_sources"), "DIMENSION_SOURCES_JSON")
    metric_definitions_payload = load_json_config(event.get("metric_definitions"), "METRIC_DEFINITIONS_JSON")

    dimension_sources = {
        dimension_name: DimensionSource(
            table_name=source["table_name"],
            attribute_name=source["attribute_name"],
        )
        for dimension_name, source in dimension_sources_payload.items()
    }
    metric_definitions = [
        MetricDefinition(
            metric_name=definition["metric_name"],
            dimension_name=definition["dimension_name"],
        )
        for definition in metric_definitions_payload
    ]

    return Settings(
        aws_region=os.environ["AWS_REGION"],
        cloudwatch_namespace=str(event.get("cloudwatch_namespace") or os.environ["CLOUDWATCH_NAMESPACE"]),
        export_env=str(event.get("export_env") or os.environ["EXPORT_ENV"]),
        s3_bucket=str(event.get("s3_bucket") or os.environ["S3_BUCKET"]),
        s3_prefix=str(event.get("s3_prefix") or os.environ["S3_PREFIX"]),
        months_back=int(event.get("months_back") or os.environ["MONTHS_BACK"]),
        period_seconds=int(event.get("period_seconds") or os.environ["PERIOD_SECONDS"]),
        dimension_sources=dimension_sources,
        metric_definitions=metric_definitions,
    )


def build_s3_key(
    prefix: str,
    export_env: str,
    metric_name: str,
    dimension_name: str,
    dimension_value: str,
    end_time: datetime,
) -> str:
    return (
        f"{prefix.strip('/')}/env={export_env}/"
        f"metric_name={metric_name}/"
        f"date={end_time.date()}/"
        f"dimension_name={dimension_name}/"
        f"dimension_value={quote(dimension_value, safe='')}/"
        "data.json"
    )


def list_dimension_values(
    dynamodb_client: Any,
    dimension_source: DimensionSource,
) -> list[str]:
    paginator = dynamodb_client.get_paginator("scan")
    values: set[str] = set()

    for page in paginator.paginate(
        TableName=dimension_source.table_name,
        ProjectionExpression="#dimension_value",
        ExpressionAttributeNames={"#dimension_value": dimension_source.attribute_name},
    ):
        for item in page.get("Items", []):
            value = item.get(dimension_source.attribute_name, {}).get("S")
            if value:
                values.add(value)

    return sorted(values)


def build_export_body(rows: list[str]) -> bytes:
    return "\n".join(rows).encode("utf-8")


def export_metric(
    cloudwatch_client: Any,
    s3_client: Any,
    bucket: str,
    key: str,
    namespace: str,
    metric_name: str,
    dimension_name: str,
    dimension_value: str,
    start_time: datetime,
    end_time: datetime,
    period_seconds: int,
) -> int:
    next_token: str | None = None
    rows: list[str] = []

    while True:
        request: dict[str, Any] = {
            "MetricDataQueries": [
                {
                    "Id": "m1",
                    "MetricStat": {
                        "Metric": {
                            "Namespace": namespace,
                            "MetricName": metric_name,
                            "Dimensions": [
                                {
                                    "Name": dimension_name,
                                    "Value": dimension_value,
                                }
                            ],
                        },
                        "Period": period_seconds,
                        "Stat": "Sum",
                    },
                    "ReturnData": True,
                }
            ],
            "StartTime": start_time,
            "EndTime": end_time,
            "ScanBy": "TimestampAscending",
        }

        if next_token:
            request["NextToken"] = next_token

        response = cloudwatch_client.get_metric_data(**request)
        result = response.get("MetricDataResults", [{}])[0]

        for timestamp, value in zip(result.get("Timestamps", []), result.get("Values", [])):
            rows.append(
                json.dumps(
                    {
                        "metric_name": metric_name,
                        "dimension_name": dimension_name,
                        "dimension_value": dimension_value,
                        "timestamp": timestamp.astimezone(timezone.utc).isoformat().replace("+00:00", "Z"),
                        "value": value,
                    },
                    separators=(",", ":"),
                )
            )

        next_token = response.get("NextToken")
        if not next_token:
            break

    if not rows:
        return 0

    s3_client.put_object(Bucket=bucket, Key=key, Body=build_export_body(rows))
    LOGGER.info("⬆️ Uploaded %s rows to s3://%s/%s", len(rows), bucket, key)
    return len(rows)


def lambda_handler(event: dict[str, Any] | None, context: Any) -> dict[str, Any]:
    del context
    payload = event if isinstance(event, dict) else {}

    try:
        settings = load_settings(payload)
        start_time, end_time = resolve_time_range(settings.months_back, payload.get("reference_time"))

        session = boto3.Session(region_name=settings.aws_region)
        cloudwatch_client = session.client("cloudwatch", region_name=settings.aws_region)
        dynamodb_client = session.client("dynamodb", region_name=settings.aws_region)
        s3_client = session.client("s3", region_name=settings.aws_region)

        LOGGER.info("🚀 Starting metrics archive for namespace %s", settings.cloudwatch_namespace)
        LOGGER.info("ℹ️ Export window: %s -> %s", start_time.isoformat(), end_time.isoformat())

        exported_files = 0
        exported_rows = 0
        dimension_values_cache: dict[str, list[str]] = {}

        for definition in settings.metric_definitions:
            LOGGER.info("📦 Processing %s / %s", definition.metric_name, definition.dimension_name)

            dimension_values = dimension_values_cache.get(definition.dimension_name)
            if dimension_values is None:
                dimension_source = settings.dimension_sources[definition.dimension_name]
                dimension_values = list_dimension_values(
                    dynamodb_client=dynamodb_client,
                    dimension_source=dimension_source,
                )
                dimension_values_cache[definition.dimension_name] = dimension_values

            if not dimension_values:
                LOGGER.warning("⚠️ No dimension values found for %s / %s", definition.metric_name, definition.dimension_name)
                continue

            for dimension_value in dimension_values:
                key = build_s3_key(
                    prefix=settings.s3_prefix,
                    export_env=settings.export_env,
                    metric_name=definition.metric_name,
                    dimension_name=definition.dimension_name,
                    dimension_value=dimension_value,
                    end_time=end_time,
                )
                exported_count = export_metric(
                    cloudwatch_client=cloudwatch_client,
                    s3_client=s3_client,
                    bucket=settings.s3_bucket,
                    key=key,
                    namespace=settings.cloudwatch_namespace,
                    metric_name=definition.metric_name,
                    dimension_name=definition.dimension_name,
                    dimension_value=dimension_value,
                    start_time=start_time,
                    end_time=end_time,
                    period_seconds=settings.period_seconds,
                )

                if exported_count > 0:
                    exported_files += 1
                    exported_rows += exported_count

        LOGGER.info("✅ Completed export. Files: %s. Rows: %s.", exported_files, exported_rows)
        return {
            "statusCode": 200,
            "exported_files": exported_files,
            "exported_rows": exported_rows,
            "bucket": settings.s3_bucket,
            "prefix": settings.s3_prefix,
        }
    except Exception as error:
        LOGGER.exception("❌ Metrics archive failed")
        return {
            "statusCode": 500,
            "error": str(error),
        }