resource "random_integer" "assertion_bucket_suffix" {
  min = 1000
  max = 9999
}

module "kms_assertions_bucket" {
  source  = "terraform-aws-modules/kms/aws"
  version = "3.0.0"

  description             = "KMS key for S3 encryption"
  key_usage               = "ENCRYPT_DECRYPT"
  enable_key_rotation     = var.assertion_bucket.enable_key_rotation
  multi_region            = var.assertion_bucket.kms_multi_region
  enable_default_policy   = true
  rotation_period_in_days = var.kms_rotation_period_in_days

  key_statements = var.assertion_bucket.lambda_role_arn != null ? [
    {
      sid = "CrossAccountLambda"
      actions = [
        "kms:GenerateDataKey"
      ]
      resources = ["*"]

      principals = [
        {
          type        = "AWS"
          identifiers = [var.assertion_bucket.lambda_role_arn]
        }
      ]
    }
  ] : []

  # Aliases
  aliases = ["assertions/S3"]
}

resource "aws_iam_role" "replication" {
  count = var.assertion_bucket.replication_configuration != null ? 1 : 0

  name               = "${var.role_prefix}-replica-assertions"
  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "s3.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
POLICY
}

resource "aws_iam_policy" "replication" {
  count = var.assertion_bucket.replication_configuration != null ? 1 : 0
  name  = "${var.role_prefix}-replica-assertions"

  policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "s3:ListBucket",
        "s3:GetReplicationConfiguration",
        "s3:GetObjectVersionForReplication",
        "s3:GetObjectVersionAcl",
        "s3:GetObjectVersionTagging",
        "s3:GetObjectRetention",
        "s3:GetObjectLegalHold"
      ],
      "Effect": "Allow",
      "Resource": [
        "${module.s3_assertions_bucket.s3_bucket_arn}",
        "${module.s3_assertions_bucket.s3_bucket_arn}/*"
      ]
    },
    {
      "Action": [
        "s3:ReplicateObject",
        "s3:ReplicateDelete",
        "s3:ReplicateTags",
        "s3:GetObjectVersionTagging",
        "s3:ObjectOwnerOverrideToBucketOwner"
      ],
      "Effect": "Allow",
      "Condition": {
        "StringLikeIfExists": {
          "s3:x-amz-server-side-encryption": [
            "aws:kms",
            "aws:kms:dsse",
            "AES256"
          ]
        }
      },
      "Resource": [
        "${var.assertion_bucket.replication_configuration.destination_bucket_arn}/*"
      ]
    },
    {
      "Action": [
        "kms:Decrypt"
      ],
      "Effect": "Allow",
      "Condition": {
        "StringLike": {
          "kms:ViaService": "s3.eu-south-1.amazonaws.com",
          "kms:EncryptionContext:aws:s3:arn": [
            "${module.s3_assertions_bucket.s3_bucket_arn}/*"
          ]
        }
      },
      "Resource": [
        "${module.kms_assertions_bucket.aliases["assertions/S3"].target_key_arn}"
      ]
    },
    {
      "Action": [
        "kms:Encrypt"
      ],
      "Effect": "Allow",
      "Condition": {
        "StringLike": {
          "kms:ViaService": [
            "s3.eu-central-1.amazonaws.com"
          ],
          "kms:EncryptionContext:aws:s3:arn": [
            "${var.assertion_bucket.replication_configuration.destination_bucket_arn}/*"
          ]
        }
      },
      "Resource": [
        "${var.assertion_bucket.replication_configuration.kms_key_replica_arn}"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "kms:Decrypt",
        "kms:GenerateDataKey"
      ],
      "Resource": [
        "${module.kms_assertions_bucket.aliases["assertions/S3"].target_key_arn}"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "kms:GenerateDataKey",
        "kms:Encrypt"
      ],
      "Resource": [
        "${var.assertion_bucket.replication_configuration.kms_key_replica_arn}"
      ]
    }
  ]
}
POLICY
}

resource "aws_iam_policy_attachment" "replication" {
  count      = var.assertion_bucket.replication_configuration != null ? 1 : 0
  name       = "${var.role_prefix}-replicate-assertions"
  roles      = [aws_iam_role.replication[0].name]
  policy_arn = aws_iam_policy.replication[0].arn
}


data "aws_iam_policy_document" "lambda_assertions" {

  count = var.assertion_bucket.lambda_role_arn != null ? 1 : 0

  statement {
    sid    = "Cross lambda access"
    effect = "Allow"
    resources = [
      "${module.s3_assertions_bucket.s3_bucket_arn}",
      "${module.s3_assertions_bucket.s3_bucket_arn}/*"
    ]
    actions = ["s3:PutObject"]

    principals {
      type = "AWS"
      identifiers = [
        "${var.assertion_bucket.lambda_role_arn}"
      ]
    }
  }
}

module "s3_assertions_accesslogs_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.bucket_accesslogs_name
  # apparently if the control_object_ownership is true it's not possible to set the ACL
  # https://stackoverflow.com/questions/76049290/error-accesscontrollistnotsupported-when-trying-to-create-a-bucket-acl-in-aws
  #acl    = "private"

  control_object_ownership = true

  attach_access_log_delivery_policy = true

  access_log_delivery_policy_source_accounts = [var.account_id]
  access_log_delivery_policy_source_buckets  = ["arn:aws:s3:::${local.assertions_bucket_name}"]

  versioning = {
    enabled = true
  }

  lifecycle_rule = [
    {
      id      = "expire_rule"
      enabled = true

      filter = {
        prefix = "/"
      }
      expiration = {
        days = var.assertion_accesslogs_expiration
      }
      noncurrent_version_expiration = {
        noncurrent_days = var.assertion_accesslogs_expiration
      }
    }
  ]

  tags = {
    Name = local.bucket_accesslogs_name
  }
}

module "s3_assertions_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.assertions_bucket_name
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "BucketOwnerEnforced"

  attach_policy = var.assertion_bucket.lambda_role_arn != null ? true : false
  policy        = var.assertion_bucket.lambda_role_arn == null ? null : data.aws_iam_policy_document.lambda_assertions[0].json

  server_side_encryption_configuration = {
    rule = {
      bucket_key_enabled = true
      apply_server_side_encryption_by_default = {
        kms_master_key_id = module.kms_assertions_bucket.aliases["assertions/S3"].arn
        sse_algorithm     = "aws:kms"
      }
    }
  }

  versioning = {
    enabled    = true
    mfa_delete = var.assertion_bucket.mfa_delete
  }

  object_lock_enabled       = var.assertion_bucket.object_lock_configuration != null ? true : false
  object_lock_configuration = var.assertion_bucket.object_lock_configuration

  lifecycle_rule = [
    {
      enabled = true
      id      = "expire_rule"
      prefix  = ""
      tags    = {}

      expiration = {
        days = var.assertion_bucket.expiration_days
      }
    }
  ]

  replication_configuration = local.replication_configuration

  logging = {
    target_bucket = module.s3_assertions_accesslogs_bucket.s3_bucket_id
    target_prefix = "/"
  }
  tags = {
    Name   = local.assertions_bucket_name
    Backup = "True"
  }
}

## Athena ##

module "s3_athena_output_bucket" {
  count   = var.create_athena_table ? 1 : 0
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.athena_outputs
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  server_side_encryption_configuration = {
    rule = {
      bucket_key_enabled = true
      apply_server_side_encryption_by_default = {
        kms_master_key_id = module.kms_assertions_bucket.aliases["assertions/S3"].arn
        sse_algorithm     = "aws:kms"
      }
    }
  }

  tags = {
    Name = local.assertions_bucket_name
  }
}

resource "aws_athena_workgroup" "assertions_workgroup" {
  count = var.create_athena_table ? 1 : 0
  name  = "assertions_workgroup"

  configuration {
    result_configuration {
      output_location = "s3://${local.athena_outputs}/output/"
    }
  }
}

# Create Athena database
resource "aws_athena_database" "assertions" {
  count  = var.create_athena_table ? 1 : 0
  name   = "assertions"
  bucket = module.s3_athena_output_bucket[0].s3_bucket_id
}

data "aws_iam_policy_document" "glue_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["glue.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "glue_assertions" {
  count              = var.create_athena_table ? 1 : 0
  name               = "AWSGlueServiceRole-Assertions"
  assume_role_policy = data.aws_iam_policy_document.glue_assume_role_policy.json
  path               = "/service-role/"
}

data "aws_iam_policy_document" "glue_assertions_policy" {
  statement {
    sid       = "S3ReadAndWrite"
    effect    = "Allow"
    resources = ["arn:aws:s3:::${module.s3_assertions_bucket.s3_bucket_id}/*"]
    actions   = ["s3:GetObject", "s3:PutObject"]
  }

  statement {
    sid       = "KMSDecryptEncryptAsserions"
    effect    = "Allow"
    resources = [module.kms_assertions_bucket.aliases["assertions/S3"].target_key_arn]
    actions   = ["kms:Decrypt", "kms:Encrypt"]
  }
}

resource "aws_iam_policy" "glue_assertions_policy" {
  count       = var.create_athena_table ? 1 : 0
  name        = "AWSGlueServiceRoleAssertionsS3Policy"
  description = "S3 bucket assertions policy for glue."
  policy      = data.aws_iam_policy_document.glue_assertions_policy.json
}

locals {
  glue_assertions_policy = compact([
    "arn:aws:iam::aws:policy/service-role/AWSGlueServiceRole",
    var.create_athena_table ? aws_iam_policy.glue_assertions_policy[0].arn : null
  ])
}

resource "aws_iam_role_policy_attachment" "glue_s3_assertions_policy" {
  count      = var.create_athena_table ? length(local.glue_assertions_policy) : 0
  role       = aws_iam_role.glue_assertions[0].name
  policy_arn = local.glue_assertions_policy[count.index]
  depends_on = [aws_iam_policy.glue_assertions_policy]
}

resource "aws_glue_catalog_database" "assertions" {
  count = var.create_athena_table == true ? 1 : 0
  name  = "assertions"
}

resource "aws_glue_crawler" "assertions" {
  count         = var.create_athena_table == true ? 1 : 0
  database_name = aws_glue_catalog_database.assertions[0].name
  name          = "assertions"
  role          = aws_iam_role.glue_assertions[0].arn

  schedule = var.assertions_crawler_schedule

  description = "Crawler for the assertions bucket"

  configuration = jsonencode(
    {
      CrawlerOutput = {
        Tables = {
          TableThreshold = 1
        }
      }
      CreatePartitionIndex = true
      Version              = 1.0
    }
  )

  s3_target {
    path = "s3://${module.s3_assertions_bucket.s3_bucket_id}/"
  }
}
