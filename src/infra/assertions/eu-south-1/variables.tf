variable "aws_region" {
  type        = string
  description = "AWS region to create resources. Default Milan"
  default     = "eu-south-1"
}

variable "aws_region_short" {
  type        = string
  description = "AWS region short format."
  default     = "es-1"
}

variable "app_name" {
  type        = string
  description = "App name."
  default     = "oneid"
}

variable "env_short" {
  type        = string
  default     = "p"
  description = "Environment short."
}

## Storage S3 ## 
variable "assertion_bucket" {
  type = object({
    mfa_delete                = bool
    glacier_transaction_days  = number
    expiration_days           = number
    kms_multi_region          = bool
    enable_key_rotation       = bool
    object_lock_configuration = any
    replication_configuration = optional(
      object({
        id                     = string
        destination_bucket_arn = string
        kms_key_replica_arn    = string
    }), null)
    lambda_role_arn = optional(string, null)

  })

  description = "Assertion storage."
  default = {
    mfa_delete               = false
    glacier_transaction_days = 90
    expiration_days          = 731
    enable_key_rotation      = true
    kms_multi_region         = false
    object_lock_configuration = {
      rule = {
        default_retention = {
          mode = "GOVERNANCE"
          days = 730 #
        }
      }
    }
    lambda_role_arn = "arn:aws:iam::851725347804:role/oneid-es-1-p-assertion"

    /*
    replication_configuration = {
      id                     = "eu-south-1-to-eu-central-1"
      destination_bucket_arn = "arn:aws:s3:::assertions-3786"
      kms_key_replica_arn    = "arn:aws:kms:eu-central-1:851725347804:key/mrk-b92f2476079142188f1664e0b4a5150a"
    }
    */
  }

}

variable "xsw_assertions_bucket" {
  type = object({
    mfa_delete               = bool
    glacier_transaction_days = number
    expiration_days          = number
    enable_key_rotation      = bool
    kms_multi_region         = bool
  })

  description = "XSW assertions bucket configurations."
  default = {
    mfa_delete               = false
    glacier_transaction_days = 90
    expiration_days          = 731
    enable_key_rotation      = true
    kms_multi_region         = false
  }
}

variable "assertions_crawler_schedule" {
  type        = string
  description = "A cron expression used to specify the schedule"
  default     = "cron(00 08 ? * MON *)"
}

variable "tags" {
  type = map(any)
  default = {
    CreatedBy   = "Terraform"
    Environment = "Prod"
    Owner       = "Oneidentity"
    Source      = "https://github.com/pagopa/oneidentity"
    CostCenter  = "tier0"
  }
}
