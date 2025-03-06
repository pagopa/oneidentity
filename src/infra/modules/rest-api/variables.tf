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

variable "xray_tracing_enabled" {
  type        = bool
  description = "Whether active tracing with X-ray is enabled."
  default     = false
}

variable "api_cache_cluster_enabled" {
  type        = bool
  description = "Enablr cache cluster is enabled for the stage."
  default     = false
}

variable "api_cache_cluster_size" {
  type        = number
  description = "Size of the cache cluster for the stage, if enabled."
  default     = 0.5
}


variable "method_settings" {
  description = "List of Api Gateway method settings."
  type = list(object({
    method_path                             = string
    metrics_enabled                         = optional(bool, false)
    logging_level                           = optional(string, "OFF")
    data_trace_enabled                      = optional(bool, false)
    throttling_rate_limit                   = optional(number, -1)
    throttling_burst_limit                  = optional(number, -1)
    caching_enabled                         = optional(bool, false)
    cache_ttl_in_seconds                    = optional(number, 0)
    cache_data_encrypted                    = optional(bool, false)
    require_authorization_for_cache_control = optional(bool, false)
    cache_key_parameters                    = optional(list(string), [])
  }))
  default = []
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
    api_key_name         = optional(string, null)
  })
  description = "Name of the plan associated to the set of apis."
}


variable "api_authorizer" {
  type = object({
    name          = optional(string, "")
    user_pool_arn = optional(string, "")
  })
}