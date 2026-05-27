data "aws_iam_policy_document" "streams_assume_role" {
  count = local.metric_stream_enabled ? 1 : 0

  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["streams.metrics.cloudwatch.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "metric_stream_to_firehose" {
  count = local.metric_stream_enabled ? 1 : 0

  name               = local.metric_stream_role_name
  assume_role_policy = data.aws_iam_policy_document.streams_assume_role[0].json
}

data "aws_iam_policy_document" "metric_stream_to_firehose" {
  count = local.metric_stream_enabled ? 1 : 0

  statement {
    effect = "Allow"

    actions = [
      "firehose:PutRecord",
      "firehose:PutRecordBatch",
    ]

    resources = [aws_kinesis_firehose_delivery_stream.metrics_archiver[0].arn]
  }
}

resource "aws_iam_role_policy" "metric_stream_to_firehose" {
  count = local.metric_stream_enabled ? 1 : 0

  name   = format("%s-policy", local.metric_stream_role_name)
  role   = aws_iam_role.metric_stream_to_firehose[0].id
  policy = data.aws_iam_policy_document.metric_stream_to_firehose[0].json
}

data "aws_iam_policy_document" "firehose_assume_role" {
  count = local.metric_stream_enabled ? 1 : 0

  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["firehose.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "firehose_to_s3" {
  count = local.metric_stream_enabled ? 1 : 0

  name               = local.metric_stream_firehose_role_name
  assume_role_policy = data.aws_iam_policy_document.firehose_assume_role[0].json
}

data "aws_iam_policy_document" "firehose_to_s3" {
  count = local.metric_stream_enabled ? 1 : 0

  statement {
    effect = "Allow"

    actions = [
      "s3:AbortMultipartUpload",
      "s3:GetBucketLocation",
      "s3:GetObject",
      "s3:ListBucket",
      "s3:ListBucketMultipartUploads",
      "s3:PutObject",
    ]

    resources = [
      var.metric_stream_bucket_arn,
      "${var.metric_stream_bucket_arn}/*",
    ]
  }
}

resource "aws_iam_role_policy" "firehose_to_s3" {
  count = local.metric_stream_enabled ? 1 : 0

  name   = format("%s-policy", local.metric_stream_firehose_role_name)
  role   = aws_iam_role.firehose_to_s3[0].id
  policy = data.aws_iam_policy_document.firehose_to_s3[0].json
}

resource "aws_kinesis_firehose_delivery_stream" "metrics_archiver" {
  count = local.metric_stream_enabled ? 1 : 0

  name        = local.metric_stream_firehose_name
  destination = "extended_s3"

  extended_s3_configuration {
    role_arn           = aws_iam_role.firehose_to_s3[0].arn
    bucket_arn         = var.metric_stream_bucket_arn
    compression_format = "UNCOMPRESSED"
    custom_time_zone   = "UTC"
    file_extension     = ".json"
    prefix = format(
      "%s/env=%s/metric_name=!{partitionKeyFromQuery:metric_name}/date=!{partitionKeyFromQuery:date}/dimension_name=!{partitionKeyFromQuery:dimension_name}/dimension_value=!{partitionKeyFromQuery:dimension_value}/",
      local.metric_stream_s3_prefix,
      local.metric_stream_export_env,
    )
    error_output_prefix = format(
      "%s/errors/env=%s/!{firehose:error-output-type}/date=!{timestamp:yyyy-MM-dd}/",
      local.metric_stream_s3_prefix,
      local.metric_stream_export_env,
    )

    dynamic_partitioning_configuration {
      enabled = true
    }

    processing_configuration {
      enabled = true

      processors {
        type = "RecordDeAggregation"

        parameters {
          parameter_name  = "SubRecordType"
          parameter_value = "JSON"
        }
      }

      processors {
        type = "MetadataExtraction"

        parameters {
          parameter_name  = "JsonParsingEngine"
          parameter_value = "JQ-1.6"
        }

        parameters {
          parameter_name = "MetadataExtractionQuery"
          parameter_value = trimspace(<<-QUERY
            {
              metric_name: .metric_name,
              date: (.timestamp / 1000 | floor | strftime("%Y-%m-%d")),
              dimension_name: (if (.dimensions | length) > 0 then (.dimensions | keys[0]) else "NoDimension" end),
              dimension_value: (if (.dimensions | length) > 0 then (.dimensions[(.dimensions | keys[0])] | @uri) else "all" end)
            }
          QUERY
          )
        }
      }

      processors {
        type = "AppendDelimiterToRecord"
      }
    }
  }

  depends_on = [aws_iam_role_policy.firehose_to_s3]
}

resource "aws_cloudwatch_metric_stream" "metrics_archiver" {
  count = local.metric_stream_enabled ? 1 : 0

  name          = local.metric_stream_name
  role_arn      = aws_iam_role.metric_stream_to_firehose[0].arn
  firehose_arn  = aws_kinesis_firehose_delivery_stream.metrics_archiver[0].arn
  output_format = "json"

  include_filter {
    namespace    = var.metric_stream_namespace
    metric_names = var.metric_stream_metric_names
  }

  depends_on = [
    aws_iam_role_policy.metric_stream_to_firehose,
    aws_kinesis_firehose_delivery_stream.metrics_archiver,
  ]
}