variable "assertion_bucket" {
  type = object({
    name_prefix                     = string
    glacier_transaction_days        = number
    expiration_days                 = number
    mfa_delete                      = optional(bool, false)
    kms_key_deletion_window_in_days = optional(number, 10)
    kms_multi_region                = optional(bool, false)

    object_lock_legal_hold_status = optional(bool, false)
    object_lock_configuration     = optional(any, null)
    enable_key_rotation           = optional(bool, false)
  })
}

variable "assets_bucket_prefix" {
  type = string
}

variable "github_repository" {
  type = string
}

variable "account_id" {
  type        = string
  description = "AWS Account id."
}

variable "create_athena_table" {
  type        = bool
  description = "Create athena table, query, glue crawler and all related resources."
  default     = true
}

variable "assertions_crawler_schedule" {
  type        = string
  description = "A cron expression used to specify the schedule"
  default     = null
}
