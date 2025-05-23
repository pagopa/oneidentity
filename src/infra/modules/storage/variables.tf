variable "role_prefix" {
  type        = string
  description = "Prefix to assign to the roles."
}

variable "kms_rotation_period_in_days" {
  type    = number
  default = 365
}

variable "assertion_bucket" {
  type = object({
    name_prefix                     = string
    expiration_days                 = number
    mfa_delete                      = optional(bool, false)
    kms_key_deletion_window_in_days = optional(number, 10)
    kms_multi_region                = optional(bool, false)

    object_lock_legal_hold_status = optional(bool, false)
    object_lock_configuration     = optional(any, null)
    enable_key_rotation           = optional(bool, false)
    replication_configuration = optional(
      object({
        id                     = string
        destination_bucket_arn = string
        kms_key_replica_arn    = string
    }), null)

    lambda_role_arn = optional(string, null)
  })
}

variable "create_assets_bucket" {
  type        = bool
  description = "Create assets bucket."
  default     = true
}
variable "create_assets_control_panel_bucket" {
  type        = bool
  description = "Create assets control panel bucket."
  default     = true
}

variable "assets_bucket_prefix" {
  type        = string
  description = "Assets bucket prefix"
  default     = ""
}

variable "assets_bucket_control_panel_prefix" {
  type        = string
  description = "Assets bucket control panel prefix"
  default     = ""
}

variable "create_idp_metadata_bucket" {
  type        = bool
  description = "Create idp metadata bucket."
  default     = true
}

variable "idp_metadata_bucket_prefix" {
  type        = string
  description = "Idp metadata bucket prefix."
  default     = ""
}

variable "create_metadata_bucket" {
  type        = bool
  description = "Create metadata bucket."
  default     = true
}

variable "metadata_bucket_prefix" {
  type        = string
  description = "Metadata bucket prefix."
  default     = ""
}

//TODO check if this is needed
//variable "metadata_bucket" {
//  type = object({
//    name_prefix                     = string
//    expiration_days                 = number
//    mfa_delete                      = optional(bool, false)
//    kms_key_deletion_window_in_days = optional(number, 10)
//    kms_multi_region                = optional(bool, false)
//
//    lambda_role_arn = optional(string, null)
//  })
//}

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

variable "assertion_accesslogs_expiration" {
  type = number
}

