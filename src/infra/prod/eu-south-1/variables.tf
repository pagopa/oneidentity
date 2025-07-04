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

variable "app_log_level" {
  type        = string
  description = "Log level of application"
  default     = "DEBUG" //TODO: switch to INFO after stable release
}

variable "app_cloudwatch_custom_metric_namespace" {
  type        = string
  description = "Custom metric namespace for cloudwatch"
  default     = "ApplicationMetrics"
}

variable "env_short" {
  type        = string
  default     = "p"
  description = "Environment short."
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
  default     = false
  description = "Create nat gateway(s)"
}

variable "single_nat_gateway" {
  type        = bool
  default     = false
  description = "Create a single nat gateway to spare money."
}

## R53 DNS zone ##
variable "r53_dns_zone" {
  type = object({
    name    = string
    comment = string
  })

  default = {
    name    = "oneid.pagopa.it"
    comment = "Oneidentity prod hosted zone."
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
    image_version    = "05d9825a488fc80c2b8cd08d69e55732d4762977"
    cpu              = 2048
    memory           = 4096
    container_cpu    = 2048
    container_memory = 4096
    autoscaling = {
      enable        = true
      desired_count = 3
      min_capacity  = 3
      max_capacity  = 12
    }
    logs_retention_days = 30
  }
}

variable "lambda_cloudwatch_logs_retention_in_days" {
  type        = number
  description = "Cloudwatch log group retention days."
  default     = 30
}

variable "dlq_assertion_setting" {
  type = object({
    maximum_retry_attempts        = number
    maximum_record_age_in_seconds = number
  })

  default = {
    maximum_retry_attempts        = 3
    maximum_record_age_in_seconds = 604800 # 7 days.
  }
}

## Storage S3 ## 
variable "assertion_bucket" {
  type = object({
    mfa_delete                = bool
    glacier_transaction_days  = number
    expiration_days           = number
    kms_multi_region          = bool
    enable_key_rotation       = bool
    object_lock_configuration = any
    replication_configuration = optional(
      object({
        id                     = string
        destination_bucket_arn = string
        kms_key_replica_arn    = string
    }), null)
  })

  description = "Assertion storage."
  default = {
    mfa_delete               = false
    glacier_transaction_days = 90
    expiration_days          = 731
    enable_key_rotation      = true
    kms_multi_region         = true
    object_lock_configuration = {
      rule = {
        default_retention = {
          mode = "GOVERNANCE"
          days = 730 #
        }
      }
    }

    replication_configuration = {
      id                     = "eu-south-1-to-eu-central-1"
      destination_bucket_arn = "arn:aws:s3:::assertions-3786"
      kms_key_replica_arn    = "arn:aws:kms:eu-central-1:851725347804:key/mrk-b92f2476079142188f1664e0b4a5150a"
    }
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
  default     = 10
}

variable "repository_image_tag_mutability" {
  type        = string
  description = "The tag mutability setting for the repository. Must be one of: MUTABLE or IMMUTABLE. Defaults to IMMUTABLE"
  default     = "MUTABLE"
}

variable "idp_status_history_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
  })
  description = "IDP Status History configurations table."
  default = {
    point_in_time_recovery_enabled = false
  }
}

variable "client_status_history_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
  })
  description = "Client Status History configurations table."
  default = {
    point_in_time_recovery_enabled = false
  }
}

variable "cie_entity_id" {
  type    = string
  default = "https://idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO"
}

## Metadata Info variables##
variable "metadata_info" {
  type = object({
    acs_url = string
    slo_url = string
  })

  default = {
    acs_url = "/saml/acs"
    slo_url = "/saml/slo"
  }
}


## Database ##
variable "sessions_table" {
  type = object({
    ttl_enabled                    = bool
    point_in_time_recovery_enabled = bool
    stream_enabled                 = bool
    stream_view_type               = string
    deletion_protection_enabled    = optional(bool, false)
  })
  description = "Saml responses table configurations."
  default = {
    ttl_enabled                    = true
    point_in_time_recovery_enabled = false
    stream_enabled                 = true
    stream_view_type               = "NEW_IMAGE"
    deletion_protection_enabled    = true
  }
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
  description = "Client configurations table."
  default = {
    point_in_time_recovery_enabled = true
    stream_enabled                 = true
    stream_view_type               = "NEW_AND_OLD_IMAGES"
    deletion_protection_enabled    = true
    replication_regions = [
      {
        region_name    = "eu-central-1"
        propagate_tags = true
      }
    ]
  }
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

  description = "IDP Metadata configurations table."
  default = {
    point_in_time_recovery_enabled = true
    stream_enabled                 = true
    stream_view_type               = "NEW_AND_OLD_IMAGES"
    deletion_protection_enabled    = true
    replication_regions = [
      {
        region_name    = "eu-central-1"
        propagate_tags = true
      }
    ]
  }
}

variable "last_idp_used_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
  })
  description = "Last IDP used table configurations."
  default = {
    point_in_time_recovery_enabled = false
  }
}

# DNS
variable "dns_record_ttl" {
  type        = number
  description = "Dns record ttl (in sec)"
  default     = 86400 # 24 hour.
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
      caching_enabled = false
      metrics_enabled = true
      logging_level   = "ERROR"
    },
    {
      method_path          = "saml/{id_type}/metadata/GET"
      caching_enabled      = true
      cache_ttl_in_seconds = 3600
      metrics_enabled      = true
      logging_level        = "ERROR"
    },
    {
      method_path          = "static/{proxy+}/GET"
      caching_enabled      = true
      cache_ttl_in_seconds = 3600
      logging_level        = "ERROR"
    },
    {
      method_path          = "assets/{proxy+}/GET"
      caching_enabled      = true
      cache_ttl_in_seconds = 3600
      logging_level        = "ERROR"
    },
    {
      method_path          = "login/GET"
      caching_enabled      = true
      cache_ttl_in_seconds = 3600
      logging_level        = "ERROR"
    },
    {
      method_path          = "login/error/GET"
      caching_enabled      = true
      cache_ttl_in_seconds = 3600
      logging_level        = "ERROR"
    },
    {
      method_path          = "idps/GET"
      caching_enabled      = true
      metrics_enabled      = true
      cache_ttl_in_seconds = 3600
      logging_level        = "ERROR"
    },
    {
      method_path          = ".well-known/openid-configuration/GET"
      caching_enabled      = true
      cache_ttl_in_seconds = 3600
      logging_level        = "ERROR"
    },
    {
      method_path          = "clients/{client_id}/GET"
      caching_enabled      = true
      metrics_enabled      = true
      cache_ttl_in_seconds = 3600
      logging_level        = "ERROR"
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
    rate_limit  = 300
    burst_limit = 500
  }
}

variable "alarm_subscribers" {
  type    = string
  default = "alarm-subscribers"
}

variable "is_gh_sns_arn" {
  type    = string
  default = "arn:aws:sns:eu-south-1:116453376486:history"
}

variable "ssm_cert_key" {
  type = object({
    cert_pem = string
    key_pem  = string
  })
  default = {
    cert_pem = "cert.pem"
    key_pem  = "key.pem"
  }
}

variable "ecs_alarms" {
  type = map(object({
    metric_name         = string
    namespace           = string
    threshold           = optional(number)
    evaluation_periods  = optional(number)
    period              = optional(number)
    statistic           = optional(string)
    comparison_operator = optional(string)
    scaling_policy      = optional(string)
  }))

  default = {
    "cpu_high" = {
      metric_name         = "CPUUtilization"
      namespace           = "AWS/ECS"
      evaluation_periods  = 1
      comparison_operator = "GreaterThanOrEqualToThreshold"
      threshold           = 50
      period              = 60
      statistic           = "Average"
      scaling_policy      = "cpu_high"
    },
    "cpu_low" = {
      metric_name         = "CPUUtilization"
      namespace           = "AWS/ECS"
      evaluation_periods  = 3
      comparison_operator = "LessThanOrEqualToThreshold"
      threshold           = 20
      period              = 900
      statistic           = "Average"
      scaling_policy      = "cpu_low"
    },
    "mem_high" = {
      metric_name         = "MemoryUtilization"
      namespace           = "AWS/ECS"
      evaluation_periods  = 1
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 60
      statistic           = "Average"
      threshold           = 70
    }
  }
}

variable "lambda_alarms" {
  type = map(object({
    metric_name         = optional(string, "Errors")
    namespace           = optional(string, "AWS/Lambda")
    threshold           = optional(number, 1)
    evaluation_periods  = optional(number, 1)
    period              = optional(number, 300)
    statistic           = optional(string, "Sum")
    comparison_operator = optional(string, "GreaterThanOrEqualToThreshold")
    treat_missing_data  = optional(string, "notBreaching")
  }))

  default = {
    "oneid-es-1-p-assertion" = {
    },
    "oneid-es-1-p-metadata" = {},
    "oneid-es-1-p-client-registration" = {
    },
    "oneid-es-1-p-update-idp-metadata"      = {},
    "oneid-es-1-p-is-gh-integration-lambda" = {}
  }
}

variable "dlq_alarms" {
  type = object({
    metric_name         = string
    namespace           = string
    threshold           = optional(number)
    evaluation_periods  = optional(number)
    period              = optional(number)
    statistic           = optional(string)
    comparison_operator = optional(string)
    sns_topic_alarm_arn = optional(list(string))
  })

  default = {

    metric_name         = "ApproximateNumberOfMessagesVisible"
    namespace           = "AWS/SQS"
    threshold           = 0
    evaluation_periods  = 1
    comparison_operator = "GreaterThanThreshold"
    period              = 300
    statistic           = "Sum"

  }
}

variable "api_alarms" {
  type = map(object({
    metric_name         = string
    namespace           = string
    threshold           = optional(number)
    evaluation_periods  = optional(number)
    period              = optional(number)
    statistic           = optional(string)
    comparison_operator = optional(string)
    resource_name       = string
    method              = string

  }))

  default = {
    "assertion-5xx-error" = {
      resource_name       = "/saml/assertion"
      metric_name         = "5XXError"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Sum"
      threshold           = 1
      method              = "GET"
    },
    "assertion-latency-alarm" = {
      resource_name       = "/saml/assertion"
      metric_name         = "Latency"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Average"
      method              = "GET"
      threshold           = 1000
    },
    "acs-5xx-error" = {
      resource_name       = "/saml/acs"
      metric_name         = "5XXError"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Sum"
      threshold           = 1
      method              = "POST"
    },
    "acs-latency-alarm" = {
      resource_name       = "/saml/acs"
      metric_name         = "Latency"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Average"
      method              = "POST"
      threshold           = 1000
    },
    "oidc-token-5xx-error" = {
      resource_name       = "/oidc/token"
      metric_name         = "5XXError"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Sum"
      threshold           = 1
      method              = "POST"
    },
    "oidc-token-latency-alarm" = {
      resource_name       = "/oidc/token"
      metric_name         = "Latency"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Average"
      method              = "POST"
      threshold           = 1000
    },
    "oidc-keys-5xx-error" = {
      resource_name       = "/oidc/keys"
      metric_name         = "5XXError"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Sum"
      threshold           = 1
      method              = "GET"
    },
    "oidc-keys-latency-alarm" = {
      resource_name       = "/oidc/keys"
      metric_name         = "Latency"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Average"
      method              = "GET"
      threshold           = 1000
    },
    "oidc-authorize-5xx-error" = {
      resource_name       = "/oidc/authorize"
      metric_name         = "5XXError"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Sum"
      threshold           = 1
      method              = "GET"
    },
    "oidc-authorize-latency-alarm" = {
      resource_name       = "/oidc/authorize"
      metric_name         = "Latency"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Average"
      method              = "GET"
      threshold           = 1000
    },
    "oidc-register-5xx-error" = {
      resource_name       = "/oidc/register"
      metric_name         = "5XXError"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Sum"
      threshold           = 1
      method              = "POST"
    },
    "oidc-register-latency-alarm" = {
      resource_name       = "/oidc/register"
      metric_name         = "Latency"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Average"
      method              = "POST"
      threshold           = 1000
    },
    "login-5xx-error" = {
      resource_name       = "/login"
      metric_name         = "5XXError"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Sum"
      threshold           = 1
      method              = "GET"
    },
    "login-latency-alarm" = {
      resource_name       = "/login"
      metric_name         = "Latency"
      namespace           = "AWS/ApiGateway"
      evaluation_periods  = 2
      comparison_operator = "GreaterThanOrEqualToThreshold"
      period              = 300
      statistic           = "Average"
      method              = "GET"
      threshold           = 1000
    },
  }
}


variable "tags" {
  type = map(any)
  default = {
    CreatedBy   = "Terraform"
    Environment = "Prod"
    Owner       = "Oneidentity"
    Source      = "https://github.com/pagopa/oneidentity"
    CostCenter  = "tier0"
  }
}
