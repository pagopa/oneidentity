variable "aws_region" {
  type        = string
  description = "AWS Region."
}

variable "vpc_id" {
  type        = string
  description = "VPC id."
}

variable "vpc_cidr_block" {
  type        = string
  description = "VPC Cidr block. Is required when you create the application load balancer."
  default     = null
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
  default = []
}

## Network loadbalancer.

variable "nlb_dns_name" {
  type        = string
  description = "NLB dns name."
}

## Application load balancer spid test
variable "create_alb_spid_validator" {
  type        = bool
  description = "Create the ALB spid to expose the spid validator."
  default     = false
}

variable "alb_spid_validator_name" {
  type        = string
  description = "Public application load balancer name for spid validator."
}

## Lambda medatada ##  

variable "metadata_lamba_name" {
  type        = string
  description = "Lambda metadata name"
}

## Lambda metadata
variable "metadata_lamba_arn" {
  type        = string
  description = "lambda metadata arn"
}

## Lambda client registration
variable "client_registration_lambda_arn" {
  type        = string
  description = "lambda client registration arn"
}
