
variable "sessions_table" {
  type = object({
    ttl_enabled                    = optional(bool, true)
    point_in_time_recovery_enabled = optional(bool, false)
    stream_enabled                 = optional(bool, false)
    stream_view_type               = optional(string, null)
  })
  description = "Saml responses table configurations."
}

variable "client_registrations_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
    stream_enabled                 = optional(bool, false)
    stream_view_type               = optional(string, null)
    replication_regions = optional(list(object({
      region_name            = string
      propagate_tags         = optional(bool, true)
      point_in_time_recovery = optional(bool, true)
    })), [])
  })
  description = "Client registrations table configurations."
}
