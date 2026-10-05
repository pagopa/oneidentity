WITH parsed_metrics AS (
  SELECT
    COALESCE(
      try(
        from_unixtime(
          try_cast(json_extract_scalar(record, '$.timestamp') AS bigint) / 1000.0
        )
      ),
      try(
        from_iso8601_timestamp(
          json_extract_scalar(record, '$.timestamp')
        )
      )
    ) AS event_time,
    COALESCE(
      try_cast(json_extract_scalar(record, '$.value.sum') AS double),
      try_cast(json_extract_scalar(record, '$.value') AS double)
    ) AS metric_value,
    CASE
      WHEN "$path" LIKE '%/data.json' THEN 'backfill'
      ELSE 'stream'
    END AS source_type
  FROM "${database_name}"."${catalog_table_name}"
  WHERE json_extract_scalar(record, '$.metric_name') = 'IDPSuccess'
    AND "$path" NOT LIKE '%/errors/%'
),
stream_cutover AS (
  SELECT min(event_time) AS first_stream_event_time
  FROM parsed_metrics
  WHERE source_type = 'stream'
)
SELECT
  sum(metric_value) AS total_successful_idp_logins,
  min(event_time) AS first_included_event_time,
  max(event_time) AS last_included_event_time,
  count(*) AS included_records
FROM parsed_metrics
CROSS JOIN stream_cutover
WHERE metric_value IS NOT NULL
  AND cast(event_time AS date) BETWEEN DATE '2025-03-01' AND DATE '2026-08-31'
  AND (
    source_type = 'stream'
    OR event_time < first_stream_event_time
  );