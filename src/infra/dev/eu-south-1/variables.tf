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
  default     = "DEBUG"
}

variable "app_cloudwatch_custom_metric_namespace" {
  type        = string
  description = "Custom metric namespace for cloudwatch"
  default     = "ApplicationMetrics"
}

variable "env_short" {
  type        = string
  default     = "d"
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
    name    = "dev.oneid.pagopa.it"
    comment = "Oneidentity dev zone."
  }
}

## ECS Cluster ##
variable "ecs_enable_container_insights" {
  type        = bool
  description = "Enable ecs cluster container insight."
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
    event_autoscaling = optional(object({
      desired_count = number
      min_capacity  = number
      max_capacity  = number
    }), null)
    logs_retention_days   = number
    app_spid_test_enabled = optional(bool, false)
  })
  description = "Oneidentity core backend configurations."

  default = {
    image_version    = "ee2f581bd28b21011b9abb9fa98b4dd59b5ae4a9"
    cpu              = 512
    memory           = 1024
    container_cpu    = 512
    container_memory = 1024
    autoscaling = {
      enable        = true
      desired_count = 1
      min_capacity  = 1
      max_capacity  = 2
    }
    event_autoscaling = {
      desired_count = 5
      min_capacity  = 5
      max_capacity  = 10
    }
    logs_retention_days   = 30
    app_spid_test_enabled = true
  }
}

variable "ecs_oneid_internal_idp" {
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
  description = "Oneidentity Internal IdP configurations."

  default = {
    image_version    = "ee2f581bd28b21011b9abb9fa98b4dd59b5ae4a9" #todo change this
    cpu              = 512
    memory           = 1024
    container_cpu    = 512
    container_memory = 1024
    autoscaling = {
      enable        = true
      desired_count = 1
      min_capacity  = 1
      max_capacity  = 2
    }
    logs_retention_days   = 30
    app_spid_test_enabled = true
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

variable "xsw_assertions_bucket" {
  type = object({
    mfa_delete               = bool
    glacier_transaction_days = number
    expiration_days          = number
  })

  description = "XSW assertions bucket configurations."
  default = {
    mfa_delete               = false
    glacier_transaction_days = 90
    expiration_days          = 7
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
    point_in_time_recovery_enabled = false
  }
}

variable "idp_metadata_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
  })
  description = "IDP Metadata configurations table."
  default = {
    point_in_time_recovery_enabled = false
  }
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

variable "last_idp_used_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
    ttl_enabled                    = optional(bool, false)
  })
  description = "Last IDP used table configurations."
  default = {
    point_in_time_recovery_enabled = false
    ttl_enabled                    = false
  }
}

variable "internal_idp_users_table" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
  })
  description = "Internal IDP users."
  default = {
    point_in_time_recovery_enabled = false
  }
}

variable "internal_idp_sessions" {
  type = object({
    point_in_time_recovery_enabled = optional(bool, false)
  })
  description = "Internal IDP sessions table."
  default = {
    point_in_time_recovery_enabled = false
  }
}

variable "cie_entity_id" {
  type    = string
  default = "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO"
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

# DNS
variable "dns_record_ttl" {
  type        = number
  description = "Dns record ttl (in sec)"
  default     = 3600 # one minutes
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
  default     = false
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
    rate_limit  = 50
    burst_limit = 100
  }
}

variable "tags" {
  type = map(any)
  default = {
    CreatedBy   = "Terraform"
    Environment = "Dev"
    Owner       = "Oneidentity"
    Source      = "https://github.com/pagopa/oneidentity"
    CostCenter  = "tier0"
  }
}

variable "ecs_alarms" {
  type = map(object({
    metric_name         = string
    namespace           = string
    threshold           = number
    evaluation_periods  = number
    period              = number
    statistic           = string
    comparison_operator = string
    scaling_policy      = optional(string, null)

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
      threshold           = 5
      period              = 300
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
    "oneid-es-1-d-assertion" = {
    },
    "oneid-es-1-d-metadata" = {},
    "oneid-es-1-d-client-registration" = {
    },
    "oneid-es-1-d-update-idp-metadata" = {}
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
    sns_topic_alarm_arn = optional(string, null)
  })

  default = {

    metric_name         = "ApproximateNumberOfMessagesVisible"
    namespace           = "AWS/SQS"
    threshold           = 0
    evaluation_periods  = 2
    comparison_operator = "GreaterThanThreshold"
    period              = 300
    statistic           = "Sum"

  }
}


variable "alarm_subscribers" {
  type    = string
  default = "alarm-subscribers"
}

variable "event_mode" {
  type        = bool
  description = "Enable event mode to use higher autoscaling limits for special events. Set to true to activate event_autoscaling configuration."
  default     = false
}

variable "is_gh_sns_arn" {
  type    = string
  default = null
}

variable "ssm_cert_key" {
  type = object({
    cert_pem = optional(string)
    key_pem  = optional(string)
  })

  default = {
    cert_pem = "cert.pem"
    key_pem  = "key.pem"
  }

}

variable "ssm_idp_internal_cert_key" {
  type = object({
    cert_pem = optional(string)
    key_pem  = optional(string)
  })

  default = {
    cert_pem = "idp_internal_cert.pem"
    key_pem  = "idp_internal_key.pem"
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
      threshold           = 2000
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
      threshold           = 2000
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
      threshold           = 2000
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
      threshold           = 2000
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
      threshold           = 2000
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
      threshold           = 2000
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
      threshold           = 2000
    },
  }
}

variable "entity_id" {
  type = list(string)
  default = [
    "https://demo.spid.gov.it",
    "https://validator.spid.gov.it",
    "https://validator.dev.oneid.pagopa.it",
    "https://5ucp2co2zvqle6tcyrx4i5se7q0xdkni.lambda-url.eu-south-1.on.aws",
    "https://validator.dev.oneid.pagopa.it/demo",
    "https://koz3yhpkscymaqgp4m7ceguu6m0tffuz.lambda-url.eu-south-1.on.aws",
    "https://collaudo.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO",
    "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO"
  ]
}

variable "client_ids" {
  type    = list(string)
  default = ["bxMiPVktuZ5lBNbZYJ3ODosXL57ltrLp7BgyOkw-0v4"]
}

variable "pairwise_enabled" {
  type        = bool
  default     = true
  description = "Enable PDV pairwise feature"
}

variable "registry_enabled" {
  type        = bool
  default     = false
  description = "Enable PDV registry feature"
}

variable "pdv_base_url" {
  type        = string
  default     = "https://yt0fynnjbj.execute-api.eu-south-1.amazonaws.com/private-v1"
  description = "PDV base URL"
}

variable "pdv_plan_url" {
  type        = string
  default     = "https://9tmk2l5z68.execute-api.eu-south-1.amazonaws.com/private-v1"
  description = "PDV plan URL"
}