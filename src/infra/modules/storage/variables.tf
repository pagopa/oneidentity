variable "assertion_bucket" {
  type = object({
    name_prefix              = string
    gracier_transaction_days = number
    expiration_days          = number
    mfa_delete               = optional(bool, false)
  })
}