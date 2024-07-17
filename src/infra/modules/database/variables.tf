
variable "sessions_table" {
  type = object({
    ttl_enabled                    = optional(bool, true)
    point_in_time_recovery_enabled = optional(bool, false)
    stream_enabled                 = optional(bool, false)
    stream_view_type               = string
  })
  description = "Saml responses table configurations."
}