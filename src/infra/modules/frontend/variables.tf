variable "aws_region" {
  type        = string
  description = "AWS Region."
}

variable "vpc_id" {
  type        = string
  description = "VPC id."
}


variable "public_subnet_ids" {
  type        = list(string)
  description = "Public subnet ids."
}

## DNS ##
variable "r53_dns_zones" {
  type        = any
  description = "R53 DNS Zones."
}

## API Gateway ## 

variable "rest_api_name" {
  type        = string
  description = "Rest api name"

}

variable "rest_api_stage" {
  type        = string
  description = "Rest api stage name"
  default     = "v1"
}

variable "api_gateway_target_arns" {
  type        = list(string)
  description = "List of target arn for the api gateway."
}

variable "api_gateway_plan" {
  type = object({
    name                 = string
    throttle_burst_limit = number
    throttle_rate_limit  = number
  })
  description = "Name of the plan associated to the set of apis."
}

variable "nlb_dns_name" {
  type        = string
  description = "NLB dns name."
}

variable "metadata_lamba_name" {
  type        = string
  description = "lambda metadata name"
}

variable "metadata_lamba_arn" {
  type        = string
  description = "lambda metadata arn"
}

variable "api_method_settings" {
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

  default = [
    {
      method_path           = "*/*"
      metrics_enabled       = true
      logging_level         = "INFO"
    }
  ]  
}