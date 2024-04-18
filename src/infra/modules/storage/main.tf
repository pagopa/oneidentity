resource "random_integer" "assetion_bucket_suffix" {
  min = 1000
  max = 9999
}

locals {
  bucket_name = format("%s-%s", var.assertion_bucket.name_prefix,
  random_integer.assetion_bucket_suffix.result)
}

resource "aws_kms_key" "kms_assetion_bucket" {
  description = "KMS key for S3 encryption"
  # Optional, specify the number of days after which the key is deleted permanently
  deletion_window_in_days = var.assertion_bucket.kms_key_deletion_window_in_days
}


module "s3_assetion_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.bucket_name
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  server_side_encryption_configuration = {
    rule = {
      apply_server_side_encryption_by_default = {
        kms_master_key_id = aws_kms_key.kms_assetion_bucket.arn
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

      transitions = [
        {
          days          = var.assertion_bucket.gracier_transaction_days
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