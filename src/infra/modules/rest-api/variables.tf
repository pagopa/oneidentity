variable "name" {
  type        = string
  description = "Rest api name"
}


variable "body" {
  type        = string
  description = "Open api json body"
}

variable "endpoint_configuration" {
  type = object({
    types            = list(string)
    vpc_endpoint_ids = optional(list(string), null)
  })
}

variable "stage_name" {
  type        = string
  description = "Stage name."
}


variable "custom_domain_name" {
  type        = string
  description = "Api gateway custom domain name."
  default     = null
}

variable "create_custom_domain_name" {
  type        = bool
  description = "Create custom domain name. If true the custom_domain_name can not be null."
  default     = false
}

variable "certificate_arn" {
  type        = string
  description = "Api Gateway certificate arn"
  default     = null
}

variable "api_mapping_key" {
  type        = string
  description = "The API mapping key."
  default     = null
}

variable "plan" {
  type = object({
    name                 = string
    throttle_burst_limit = number
    throttle_rate_limit  = number

  })
  description = "Name of the plan associated to the set of apis."
}