resource "random_integer" "assertion_bucket_suffix" {
  min = 1000
  max = 9999
}

locals {
  bucket_name = format("%s-%s", var.assertion_bucket.name_prefix,
  random_integer.assertion_bucket_suffix.result)

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
  
  logging = {
    target_bucket = local.bucket_name
    target_prefix = "logs/"
  }

  lifecycle_rule = [
    {
      enabled = true
      id      = "glacier_rule"
      prefix  = ""
      tags    = {}

      transition = [
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
