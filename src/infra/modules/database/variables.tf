
variable "sessions_table" {
  type = object({
    ttl_enabled                    = optional(bool, true)
    point_in_time_recovery_enabled = optional(bool, false)
    stream_enabled                 = optional(bool, false)
    stream_view_type               = optional(string, null)
    deletion_protection_enabled    = optional(bool, false)
  })
  description = "Saml responses table configurations."
}

variable "client_registrations_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
    stream_enabled                 = optional(bool, false)
    stream_view_type               = optional(string, null)
    deletion_protection_enabled    = optional(bool, false)
    replication_regions = optional(list(object({
      region_name            = string
      propagate_tags         = optional(bool, true)
      point_in_time_recovery = optional(bool, true)
    })), [])
  })
  description = "Client registrations table configurations."
}

variable "idp_metadata_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
    stream_enabled                 = optional(bool, false)
    stream_view_type               = optional(string, null)
    deletion_protection_enabled    = optional(bool, false)
    replication_regions = optional(list(object({
      region_name            = string
      propagate_tags         = optional(bool, true)
      point_in_time_recovery = optional(bool, true)
    })), [])
  })
  description = "IDP Metadata table configurations."
}
