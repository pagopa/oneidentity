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

variable "kms_ssm_enable_rotation" {
  type    = bool
  default = true
}

variable "kms_rotation_period_in_days" {
  type    = number
  default = 365
}

variable "client_registrations_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
    stream_enabled                 = optional(bool, true)
    stream_view_type               = optional(string, "NEW_AND_OLD_IMAGES")
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

variable "idp_status_history_table" {
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
  description = "IDP status history table configurations."
}

variable "idp_entity_ids" {
  type    = list(string)
  default = null
}

variable "client_status_history_table" {
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
  description = "Client status history table configurations."
}

variable "clients" {
  type = list(object({
    client_id     = string
    friendly_name = string
  }))
  default = null
}


variable "last_idp_used_table" {
  type = object({
    ttl_enabled                    = optional(bool, false)
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
  description = "Last IDP used table configurations."
}

variable "internal_idp_users_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
    stream_enabled                 = optional(bool, false)
    stream_view_type               = optional(string, null)
    deletion_protection_enabled    = optional(bool, false)
  })
  description = "Internal IDP users table."
}

variable "internal_idp_sessions" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
    stream_enabled                 = optional(bool, false)
    stream_view_type               = optional(string, null)
    deletion_protection_enabled    = optional(bool, false)
  })
  description = "Internal IDP sessions table."
}
