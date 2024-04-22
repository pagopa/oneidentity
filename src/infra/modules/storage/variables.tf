variable "assertion_bucket" {
  type = object({
    name_prefix                     = string
    gracier_transaction_days        = number
    expiration_days                 = number
    mfa_delete                      = optional(bool, false)
    kms_key_deletion_window_in_days = optional(number, 10)

    object_lock_legal_hold_status = optional(bool, false)
    object_lock_configuration     = optional(any, null)
  })
}