variable "saml_responses_table" {
  type = object({
    name                           = string
    ttl_enabled                    = optional(bool, true)
    point_in_time_recovery_enabled = optional(bool, false)
  })
  description = "Saml responses table configurations."
}


variable "client_registrations_table" {
  type = object({
    name                           = string
    point_in_time_recovery_enabled = optional(bool, false)
  })
  description = "Client configurations."
}