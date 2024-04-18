resource "random_integer" "assetion_bucket_suffix" {
  min = 1
  max = 4
}

locals {
  bucket_name = format("%s-%s", var.assertion_bucket.name_prefix,
  random_integer.assetion_bucket_suffix.result)
}


module "s3_assetion_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.bucket_name
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

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