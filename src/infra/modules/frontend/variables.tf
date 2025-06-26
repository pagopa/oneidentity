variable "aws_region" {
  type        = string
  description = "AWS Region."
}

variable "role_prefix" {
  type        = string
  description = "Prefix to assign to the IAM Roles"
}


variable "domain_name" {
  type        = string
  description = "DNS domain name."
}

variable "create_custom_domain_name" {
  type        = bool
  description = "ApiGw create custom domain admin name."
  default     = true
}

variable "domain_admin_name" {
  type        = string
  description = "DNS domain name."
}

variable "domain_internal_idp_name" {
  type        = string
  description = "DNS domain name."
  default     = null
}

variable "deploy_internal_idp_rest_api" {
  type        = bool
  description = "ApiGW deploy internal idp api."
  default     = false
}

variable "create_custom_domain_admin_name" {
  type        = bool
  description = "ApiGw create custom domain admin name."
  default     = true
}

variable "r53_dns_zone_id" {
  type        = string
  description = "R53 dns zone id."
}

variable "create_dns_record" {
  type        = bool
  description = "Create DNS record to associate the API Gateway RestApi to the hosted zone."
  default     = true
}

variable "dns_record_ttl" {
  type        = number
  description = "DNS records ttl"
}

## API Gateway ## 

variable "rest_api_name" {
  type        = string
  description = "Rest api name"
}

variable "openapi_template_file" {
  type        = string
  description = "Openapi template file path."
}

variable "rest_api_stage" {
  type        = string
  description = "Rest api stage name"
  default     = "v1"
}

variable "xray_tracing_enabled" {
  type        = bool
  description = "Whether active tracing with X-ray is enabled."
  default     = false
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
    api_key_name         = optional(string, null)
  })
  description = "Name of the plan associated to the set of apis."
}

variable "api_gateway_admin_plan" {
  type = object({
    name                 = string
    throttle_burst_limit = number
    throttle_rate_limit  = number
  })
  description = "Name of the plan associated to the set of apis."
}

variable "api_gateway_internal_idp_plan" {
  type = object({
    name                 = string
    throttle_burst_limit = number
    throttle_rate_limit  = number
  })
  description = "Name of the plan associated to the set of apis."
  default = {
    name                 = "internal-idp-plan"
    throttle_burst_limit = 1000
    throttle_rate_limit  = 1000
  }
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

variable "rest_api_admin_name" {
  type        = string
  description = "Rest api admin name"
}

variable "rest_api_internal_idp_name" {
  type        = string
  description = "Rest api internal idp name"
  default     = null
}

variable "openapi_admin_template_file" {
  type        = string
  description = "Openapi admin template file path."
}

variable "openapi_internal_idp_template_file" {
  type        = string
  description = "Openapi internal idp template file path."
  default     = null
}

variable "rest_api_admin_stage" {
  type        = string
  description = "Rest api admin stage name"
  default     = "v1"
}

variable "rest_api_internal_idp_stage" {
  type        = string
  description = "Rest api internal idp stage name"
  default     = "v1"
}

variable "cors_allow_origins" {
  type        = string
  description = "List of allowed origins for CORS."
  default     = null
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

variable "web_acl" {
  type = object({
    name                       = string
    cloudwatch_metrics_enabled = optional(bool, false)
    sampled_requests_enabled   = optional(bool, false)
    sns_topic_arn              = optional(string, "")
  })
  description = "WEB acl name"
}

## Network loadbalancer.

variable "nlb_dns_name" {
  type        = string
  description = "NLB dns name."
}

## Lambda client registration
variable "client_registration_lambda_arn" {
  type        = string
  description = "lambda client registration arn"
}

variable "client_manager_lambda_arn" {
  type        = string
  description = "lambda client manager arn"
}

## Lambda retrieve status
variable "retrieve_status_lambda_arn" {
  type        = string
  description = "lambda retrieve status arn"
}

variable "assets_bucket_arn" {
  type        = string
  description = "Assets bucket arn."
}

variable "assets_bucket_name" {
  type        = string
  description = "Assets bucket name."
}

variable "assets_control_panel_bucket_arn" {
  type        = string
  description = "Assets bucket arn."
}
variable "assets_control_panel_bucket_name" {
  type        = string
  description = "Assets bucket control panel name."
}


variable "api_alarms" {
  type = map(object({
    metric_name         = string
    namespace           = string
    threshold           = number
    evaluation_periods  = number
    period              = number
    statistic           = string
    comparison_operator = string
    resource_name       = string
    sns_topic_alarm_arn = string
    method              = string
  }))
}

variable "user_pool_arn" {
  type    = string
  default = ""
}

variable "api_authorizer_name" {
  type    = string
  default = null
}

variable "api_authorizer_admin_name" {
  type    = string
  default = null
}

variable "provider_arn" {
  type        = string
  description = "Value of the provider arn."
  default     = ""
}

