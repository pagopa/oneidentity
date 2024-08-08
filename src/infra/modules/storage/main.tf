resource "random_integer" "assertion_bucket_suffix" {
  min = 1000
  max = 9999
}

resource "random_integer" "asset_bucket_suffix" {
  min = 1000
  max = 9999
}

locals {
  bucket_name = format("%s-%s", var.assertion_bucket.name_prefix,
    random_integer.assertion_bucket_suffix.result
  )
  athena_outputs = format("query-%s", local.bucket_name)
  assets_bucket = format("%s-%s", var.assets_bucket_prefix,
  random_integer.asset_bucket_suffix.result)
}

module "kms_assertions_bucket" {
  source  = "terraform-aws-modules/kms/aws"
  version = "2.2.1"

  description         = "KMS key for S3 encryption"
  key_usage           = "ENCRYPT_DECRYPT"
  enable_key_rotation = var.assertion_bucket.enable_key_rotation

  # Aliases
  aliases = ["assertions/S3"]
}

module "s3_assets_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.assets_bucket
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  tags = {
    Name = local.assets_bucket
  }
}

resource "aws_iam_role_policy_attachment" "deploy_s3" {
  role       = aws_iam_role.githubS3deploy.name
  policy_arn = aws_iam_policy.github_s3_policy.arn
}

resource "aws_iam_role" "githubS3deploy" {
  name        = "GitHubActionS3DeployRole"
  description = "Role to assume to copy to S3."


  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          "Federated" : "arn:aws:iam::${var.account_id}:oidc-provider/token.actions.githubusercontent.com"
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringLike = {
            "token.actions.githubusercontent.com:sub" : "repo:${var.github_repository}:*"
          },
          "ForAllValues:StringEquals" = {
            "token.actions.githubusercontent.com:iss" : "https://token.actions.githubusercontent.com",
            "token.actions.githubusercontent.com:aud" : "sts.amazonaws.com"
          }
        }
      }
    ]
  })
}

resource "aws_iam_policy" "github_s3_policy" {
  name        = "github-s3-policy"
  description = "Policy to deploy to S3"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "s3:ListBucket",
          "s3:GetObject",
          "s3:PutObject"
        ],
        Resource = [
          module.s3_assets_bucket.s3_bucket_arn,
          "${module.s3_assets_bucket.s3_bucket_arn}/*"
        ]
      }
    ]
  })
}


module "s3_assertions_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.bucket_name
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

  versioning = {
    enabled    = true
    mfa_delete = var.assertion_bucket.mfa_delete
  }

  lifecycle_rule = [
    {
      enabled = true
      id      = "glacier_rule"
      prefix  = ""
      tags    = {}

      transition = [
        {
          days          = var.assertion_bucket.glacier_transaction_days
          storage_class = "GLACIER"
        }
      ]

      expiration = {
        days = var.assertion_bucket.expiration_days
      }
    }
  ]

  tags = {
    Name = local.bucket_name
  }
}

## Athena ##

module "s3_athena_output_bucket" {
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
    Name = local.bucket_name
  }
}

resource "aws_athena_workgroup" "assertions_workgroup" {
  name = "assertions_workgroup"

  configuration {
    result_configuration {
      output_location = "s3://${local.athena_outputs}/output/"
    }
  }
}

# Create Athena database
resource "aws_athena_database" "assertions" {
  name   = "assertions"
  bucket = module.s3_athena_output_bucket.s3_bucket_id
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
  name               = "AWSGlueServiceRole-Assertions"
  assume_role_policy = data.aws_iam_policy_document.glue_assume_role_policy.json
  path               = "/service-role/"

  managed_policy_arns = [
    "arn:aws:iam::aws:policy/service-role/AWSGlueServiceRole"
  ]
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
    resources = [module.kms_assertions_bucket.aliases["assertions/S3"].arn]
    actions   = ["kms:Decrypt", "kms:Encrypt"]
  }
}

resource "aws_iam_policy" "glue_assertions_policy" {
  name        = "AWSGlueServiceRoleAssertionsS3Policy"
  description = "S3 bucket assertions policy for glue."
  policy      = data.aws_iam_policy_document.glue_assertions_policy.json
}

resource "aws_iam_role_policy_attachment" "glue_s3_assertions_policy" {
  role       = aws_iam_role.glue_assertions.name
  policy_arn = aws_iam_policy.glue_assertions_policy.arn
}

resource "aws_glue_catalog_database" "assertions" {
  name = "assertions"
}

resource "aws_glue_crawler" "assertions" {
  database_name = aws_glue_catalog_database.assertions.name
  name          = "assertions"
  role          = aws_iam_role.glue_assertions.arn

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
