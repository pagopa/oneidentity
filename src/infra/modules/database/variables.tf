variable "account_id" {
  type = string
  description = "AWS account id"
  default = null
}

variable "sessions_table" {
  type = object({
    ttl_enabled                    = optional(bool, true)
    point_in_time_recovery_enabled = optional(bool, false)
    stream_enabled                 = optional(bool, false)
    stream_view_type               = string
  })
  description = "Saml responses table configurations."
}


variable "client_registrations_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
  })
  description = "Client configurations."
}


variable "eventbridge_pipe_sessions" {
  type = object({
    pipe_name = string

  })
  default = null
}