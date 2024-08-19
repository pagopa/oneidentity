variable "aws_region" {
  type        = string
  description = "AWS region to create resources. Default Milan"
  default     = "eu-south-1"
}

variable "aws_region_short" {
  type        = string
  description = "AWS region short format."
  default     = "es-1"
}

variable "app_name" {
  type        = string
  description = "App name."
  default     = "oneid"
}

variable "env_short" {
  type        = string
  default     = "u"
  description = "Evnironment short."
}

variable "vpc_cidr" {
  type        = string
  description = "VPC address space"
  default     = "10.0.0.0/17"
}

variable "vpc_private_subnets_cidr" {
  type        = list(string)
  description = "Private subnets address spaces."
  default     = ["10.0.80.0/20", "10.0.64.0/20", "10.0.48.0/20"]
}

variable "vpc_public_subnets_cidr" {
  type        = list(string)
  description = "Public subnets address spaces."
  default     = ["10.0.120.0/21", "10.0.112.0/21", "10.0.104.0/21"]
}

variable "vpc_internal_subnets_cidr" {
  type        = list(string)
  description = "Internal subnets address spaces."
  default     = ["10.0.32.0/20", "10.0.16.0/20", "10.0.0.0/20"]
}

variable "enable_nat_gateway" {
  type        = bool
  default     = true
  description = "Create nat gateway(s)"
}

variable "single_nat_gateway" {
  type        = bool
  default     = true
  description = "Create a single nat gateway to spare money."
}

## R53 DNS zone ##
variable "r53_dns_zone" {
  type = object({
    name    = string
    comment = string
  })

  default = {
    name    = "uat.oneid.pagopa.it"
    comment = "Oneidentity uat hosted zone."
  }
}

variable "ecs_enable_container_insights" {
  type        = bool
  description = "Enable ecs cluster container inight."
  default     = true
}

variable "ecs_oneid_core" {
  type = object({
    image_version    = string
    cpu              = number
    memory           = number
    container_cpu    = number
    container_memory = number
    autoscaling = object({
      enable        = bool
      desired_count = number
      min_capacity  = number
      max_capacity  = number
    })
    logs_retention_days   = number
    app_spid_test_enabled = optional(bool, false)
  })
  description = "Oneidentity core backend configurations."

  default = {
    image_version    = "83b0593b0f113eee056786850de51ecfe5079789"
    cpu              = 512
    memory           = 1024
    container_cpu    = 512
    container_memory = 1024
    autoscaling = {
      enable        = true
      desired_count = 3
      min_capacity  = 1
      max_capacity  = 6
    }
    logs_retention_days   = 30
    app_spid_test_enabled = false
  }
}

variable "lambda_cloudwatch_logs_retention_in_days" {
  type        = number
  description = "Cloudwatch log group retention days."
  default     = 14
}

variable "dlq_assertion_setting" {
  type = object({
    maximum_retry_attempts        = number
    maximum_record_age_in_seconds = number
  })

  default = {
    maximum_retry_attempts        = 3
    maximum_record_age_in_seconds = 259200
  }
}

## Storage S3 ## 
variable "assertion_bucket" {
  type = object({
    mfa_delete               = bool
    glacier_transaction_days = number
    expiration_days          = number
  })

  description = "Assertion storage configurations."
  default = {
    mfa_delete               = false
    glacier_transaction_days = 90
    expiration_days          = 100
  }

}

variable "assertions_crawler_schedule" {
  type        = string
  description = "A cron expression used to specify the schedule"
  default     = "cron(00 08 ? * MON *)"
}

## Backend ##
variable "number_of_images_to_keep" {
  type        = number
  description = "Number of images to keeps in ECR."
  default     = 5
}

variable "repository_image_tag_mutability" {
  type        = string
  description = "The tag mutability setting for the repository. Must be one of: MUTABLE or IMMUTABLE. Defaults to IMMUTABLE"
  default     = "MUTABLE"
}

## Database ##
variable "sessions_table" {
  type = object({
    ttl_enabled                    = bool
    point_in_time_recovery_enabled = bool
    stream_enabled                 = bool
    stream_view_type               = string
  })
  description = "Saml responses table configurations."
  default = {
    ttl_enabled                    = true
    point_in_time_recovery_enabled = false
    stream_enabled                 = true
    stream_view_type               = "NEW_IMAGE"
  }
}

variable "client_registrations_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
  })
  description = "Client configurations table."
  default = {
    point_in_time_recovery_enabled = true
  }
}

# DNS
variable "dns_record_ttl" {
  type        = number
  description = "Dns record ttl (in sec)"
  default     = 3600 # one hour.
}

## Api Gateway
variable "api_cache_cluster_enabled" {
  type        = bool
  description = "Enable cache cluster is enabled for the stage."
  default     = true
}

variable "api_cache_cluster_size" {
  type        = number
  description = "Size of the cache cluster for the stage, if enabled."
  default     = 0.5
}

variable "xray_tracing_enabled" {
  type        = bool
  description = "Whether active tracing with X-ray is enabled."
  default     = true
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
      method_path     = "*/*"
      metrics_enabled = true
      logging_level   = "INFO"
    },
    {
      method_path          = "saml/{id_type}/metadata/GET"
      caching_enabled      = true
      cache_ttl_in_seconds = 3600
      metrics_enabled      = true
    },
    {
      method_path          = "static/{proxy+}/GET"
      caching_enabled      = true
      cache_ttl_in_seconds = 3600
    }
  ]
}

variable "rest_api_throttle_settings" {
  type = object({
    burst_limit = number
    rate_limit  = number
  })
  description = "Rest api throttle settings."
  default = {
    rate_limit  = 200
    burst_limit = 400
  }
}

variable "tags" {
  type = map(any)
  default = {
    CreatedBy   = "Terraform"
    Environment = "Uat"
    Owner       = "Oneidentity"
    Source      = "https://github.com/pagopa/oneidentity"
    CostCenter  = "tier0"
  }
}
