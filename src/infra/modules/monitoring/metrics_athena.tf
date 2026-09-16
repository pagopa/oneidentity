locals {
  metrics_athena_config = var.metrics_athena == null ? {} : {
    metrics = var.metrics_athena
  }
}

data "aws_iam_policy_document" "metrics_glue_assume_role" {
  for_each = local.metrics_athena_config

  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["glue.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "metrics_glue" {
  for_each = local.metrics_athena_config

  name               = format("%s-role", each.value.crawler_name)
  assume_role_policy = data.aws_iam_policy_document.metrics_glue_assume_role[each.key].json
  path               = "/service-role/"
}

data "aws_iam_policy_document" "metrics_glue_s3" {
  for_each = local.metrics_athena_config

  statement {
    sid    = "ListArchivedMetrics"
    effect = "Allow"
    actions = [
      "s3:GetBucketLocation",
      "s3:ListBucket",
    ]

    resources = [each.value.raw_bucket_arn]

    condition {
      test     = "StringLike"
      variable = "s3:prefix"
      values   = [format("%s/*", trimsuffix(each.value.raw_prefix, "/"))]
    }
  }

  statement {
    sid     = "ReadArchivedMetrics"
    effect  = "Allow"
    actions = ["s3:GetObject"]

    resources = [format("%s/%s/*", each.value.raw_bucket_arn, trimsuffix(each.value.raw_prefix, "/"))]
  }
}

resource "aws_iam_role_policy" "metrics_glue_s3" {
  for_each = local.metrics_athena_config

  name   = format("%s-s3-policy", each.value.crawler_name)
  role   = aws_iam_role.metrics_glue[each.key].id
  policy = data.aws_iam_policy_document.metrics_glue_s3[each.key].json
}

resource "aws_iam_role_policy_attachment" "metrics_glue_service" {
  for_each = local.metrics_athena_config

  role       = aws_iam_role.metrics_glue[each.key].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSGlueServiceRole"
}

resource "aws_glue_catalog_database" "metrics" {
  for_each = local.metrics_athena_config

  name = each.value.database_name
}

resource "aws_glue_crawler" "metrics" {
  for_each = local.metrics_athena_config

  database_name = aws_glue_catalog_database.metrics[each.key].name
  name          = each.value.crawler_name
  role          = aws_iam_role.metrics_glue[each.key].arn
  schedule      = each.value.crawler_schedule
  table_prefix  = "metrics_"

  description = "Crawler for CloudWatch metrics archived by Firehose and backfill Lambda."

  s3_target {
    path = format("s3://%s/%s/", each.value.raw_bucket_name, trimsuffix(each.value.raw_prefix, "/"))
  }

  schema_change_policy {
    delete_behavior = "LOG"
    update_behavior = "LOG"
  }

  depends_on = [
    aws_iam_role_policy.metrics_glue_s3,
    aws_iam_role_policy_attachment.metrics_glue_service,
  ]
}

resource "aws_athena_workgroup" "metrics" {
  for_each = local.metrics_athena_config

  name = each.value.workgroup_name

  configuration {
    enforce_workgroup_configuration    = true
    publish_cloudwatch_metrics_enabled = true

    result_configuration {
      output_location = format("s3://%s/output/", each.value.results_bucket_name)

      encryption_configuration {
        encryption_option = "SSE_S3"
      }
    }
  }

  tags = {
    Name = each.value.workgroup_name
  }
}

resource "aws_athena_named_query" "client_success_total" {
  for_each = local.metrics_athena_config

  database    = aws_glue_catalog_database.metrics[each.key].name
  name        = "client-success-total"
  workgroup   = aws_athena_workgroup.metrics[each.key].name
  description = "Total ClientSuccess count from March 2025 through August 2026."
  query = templatefile("${path.module}/../../athena-query/client_success_total.sql.tpl", {
    database_name      = aws_glue_catalog_database.metrics[each.key].name
    catalog_table_name = each.value.catalog_table_name
  })
}

resource "aws_athena_named_query" "idp_success_total" {
  for_each = local.metrics_athena_config

  database    = aws_glue_catalog_database.metrics[each.key].name
  name        = "idp-success-total"
  workgroup   = aws_athena_workgroup.metrics[each.key].name
  description = "Total IDPSuccess count from March 2025 through August 2026."
  query = templatefile("${path.module}/../../athena-query/idp_success_total.sql.tpl", {
    database_name      = aws_glue_catalog_database.metrics[each.key].name
    catalog_table_name = each.value.catalog_table_name
  })
}