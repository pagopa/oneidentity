variable "account_id" {
  type        = string
  description = "AWS Account id."
}

variable "aws_region" {
  type        = string
  description = "AWS Region."
}

variable "env_short" {
  type        = string
  description = "env short"
}

variable "role_prefix" {
  type        = string
  description = "IAM Role prefix."
}

variable "ecr_registers" {
  type = list(object({
    name                            = string
    number_of_images_to_keep        = number
    repository_image_tag_mutability = optional(string, "IMMUTABLE")
  }))
  description = "ECR image repositories"
}

variable "ecs_cluster_name" {
  type        = string
  description = "ECS Cluster name"
}


variable "ssm_cert_key" {
  type = object({
    cert_pem = optional(string, "cert.pem")
    key_pem  = optional(string, "key.pem")
  })

}

variable "ssm_idp_internal_cert_key" {
  type = object({
    cert_pem = optional(string, "idp_internal_cert.pem")
    key_pem  = optional(string, "idp_internal_key.pem")
  })

}

variable "enable_container_insights" {
  type        = bool
  description = "ECS enable container insight."
  default     = true
}

variable "fargate_capacity_providers" {
  type = map(object({
    default_capacity_provider_strategy = object({
      weight = number
      base   = number
    })
  }))
}

variable "service_core" {
  type = object({
    service_name           = string
    cpu                    = number
    memory                 = number
    enable_execute_command = optional(bool, true)
    container = object({
      name                = string
      cpu                 = number
      memory              = number
      image_name          = string
      image_version       = string
      containerPort       = number
      hostPort            = number
      logs_retention_days = number
    })
    autoscaling = object({
      enable        = bool
      desired_count = number
      min_capacity  = number
      max_capacity  = number
    })
    environment_variables = list(object({
      name  = string
      value = string
    }))
  })
}

variable "service_internal_idp" {
  type = object({
    service_name           = optional(string, "")
    cpu                    = optional(number, 0)
    memory                 = optional(number, 0)
    enable_execute_command = optional(bool, true)
    container = object({
      name                = optional(string, "")
      cpu                 = optional(number, 0)
      memory              = optional(number, 0)
      image_name          = optional(string, "")
      image_version       = optional(string, "")
      containerPort       = optional(number, 0)
      hostPort            = optional(number, 0)
      logs_retention_days = optional(number, 0)
    })
    autoscaling = object({
      enable        = optional(bool, false)
      desired_count = optional(number, 0)
      min_capacity  = optional(number, 0)
      max_capacity  = optional(number, 0)
    })
    environment_variables = list(object({
      name  = optional(string, "")
      value = optional(string, "")
    }))
  })
  default = {
    service_name           = ""
    cpu                    = 0
    memory                 = 0
    enable_execute_command = true
    container = {
      name                = ""
      cpu                 = 0
      memory              = 0
      image_name          = ""
      image_version       = ""
      containerPort       = 8082
      hostPort            = 8082
      logs_retention_days = 0
    }
    autoscaling = {
      enable        = false
      desired_count = 0
      min_capacity  = 0
      max_capacity  = 0
    }
    environment_variables = []
  }
}

variable "hosted_zone_id" {
  type        = string
  description = "Hosted zone id for IAM Role"
  default     = "Z065844519UG4CA4QH19U"
}

variable "aws_caller_identity" {
  type    = string
  default = ""
}

variable "switch_region_enabled" {
  type    = bool
  default = false
}
variable "github_repository" {
  type        = string
  description = "Github repository responsible to deploy ECS tasks in the form <organization|user/repository>."
}

variable "dynamodb_table_sessions" {
  type = object({
    table_arn    = string
    gsi_code_arn = string
  })
  description = "Dynamodb table sessions anrs"
}

variable "dynamodb_table_idpMetadata" {
  type = object({
    table_arn       = string
    gsi_pointer_arn = string
  })
  description = "Dynamodb table idpMetadata anrs"
}

variable "dynamodb_table_idpStatus" {
  type = object({
    table_arn       = string
    gsi_pointer_arn = string
  })
  description = "Dynamodb table idpStatus arns"
}

variable "dynamodb_table_clientStatus" {
  type = object({
    table_arn       = string
    gsi_pointer_arn = string
  })
  description = "Dynamodb table clientStatus arns"
}

variable "dynamodb_table_internal_idp_session_arn" {
  type        = string
  description = "Arn of the dynamodb table used to store internal idp sessions."
  default     = ""
}

variable "dynamodb_table_internal_idp_users_arn" {
  type        = string
  description = "Arn of the dynamodb table used to store internal idp users."
  default     = ""
}

variable "table_client_registrations_arn" {
  type        = string
  description = "Dynamodb table client registrations arn."
}

variable "lambda_client_registration_trigger_enabled" {
  type    = bool
  default = true
}

variable "kms_sessions_table_alias_arn" {
  type        = string
  description = "Kms key used to encrypt and decrypt session table."
}

variable "kms_ssm_enable_rotation" {
  type    = bool
  default = true
}

variable "kms_rotation_period_in_days" {
  type    = number
  default = 365
}

variable "client_registration_lambda" {
  type = object({
    name                               = string
    filename                           = string
    table_client_registrations_arn     = string
    cloudwatch_logs_retention_in_days  = number
    vpc_id                             = string
    vpc_endpoint_dynamodb_prefix_id    = string
    vpc_tls_security_group_endpoint_id = string
    vpc_subnet_ids                     = list(string)
    environment_variables              = map(string)
  })

}

variable "table_last_idp_used_arn" {
  type        = string
  description = "Dynamodb table Last IDP used arn."
}

variable "metadata_lambda" {
  type = object({
    name                              = string
    filename                          = string
    table_client_registrations_arn    = string
    environment_variables             = map(string)
    assets_bucket_arn                 = string
    vpc_id                            = string
    vpc_subnet_ids                    = list(string)
    vpc_endpoint_dynamodb_prefix_id   = string
    vpc_s3_prefix_id                  = string
    vpc_endpoint_ssm_nsg_ids          = list(string)
    cloudwatch_logs_retention_in_days = number
  })

}

## Network load balancer ##
variable "nlb_name" {
  type        = string
  description = "Network load balancer name"
}

variable "internal_idp_enabled" {
  type        = bool
  description = "Deploy internal idp"
  default     = false
}

variable "vpc_id" {
  type        = string
  description = "VPC id"
}

variable "private_subnets" {
  type        = list(string)
  description = "Private subnets ids."
}

variable "vpc_cidr_block" {
  type        = string
  description = "VPC cidr block."
}


variable "dynamodb_table_stream_arn" {
  type    = string
  default = null
}

variable "dynamodb_table_stream_registrations_arn" {
  type    = string
  default = null
}

variable "dynamodb_clients_table_stream_arn" {
  type    = string
  default = null
}

variable "assertion_lambda" {
  type = object({
    name                              = string
    filename                          = string
    s3_assertion_bucket_arn           = string
    kms_assertion_key_arn             = string
    environment_variables             = map(string)
    cloudwatch_logs_retention_in_days = number
    vpc_s3_prefix_id                  = string
    vpc_tls_security_group_id         = string
    vpc_subnet_ids                    = list(string)
    vpc_id                            = string
  })
}

variable "idp_metadata_lambda" {
  type = object({
    name                              = string
    filename                          = string
    environment_variables             = map(string)
    s3_idp_metadata_bucket_arn        = string
    s3_idp_metadata_bucket_id         = string
    vpc_id                            = string
    vpc_subnet_ids                    = list(string)
    vpc_s3_prefix_id                  = string
    cloudwatch_logs_retention_in_days = number
  })

}

variable "is_gh_integration_lambda" {
  type = object({
    name                              = string
    filename                          = string
    sns_topic_arn                     = optional(string, null)
    cloudwatch_logs_retention_in_days = string
    ssm_parameter_name                = optional(string, "GH_PERSONAL_ACCESS_TOKEN")
    environment_variables             = map(string)
  })

}

variable "update_status_lambda" {
  type = object({
    name                              = string
    filename                          = string
    assets_bucket_arn                 = string
    cloudwatch_logs_retention_in_days = string
    environment_variables             = map(string)
    vpc_s3_prefix_id                  = string
    vpc_endpoint_dynamodb_prefix_id   = string
    vpc_subnet_ids                    = list(string)
    vpc_id                            = string
  })

}

variable "retrieve_status_lambda" {
  type = object({
    name                              = string
    filename                          = string
    cloudwatch_logs_retention_in_days = string
    environment_variables             = map(string)
    vpc_endpoint_dynamodb_prefix_id   = string
    vpc_subnet_ids                    = list(string)
    vpc_id                            = string
  })

}

variable "invalidate_cache_lambda" {
  type = object({
    name                              = string
    filename                          = string
    cloudwatch_logs_retention_in_days = string
    environment_variables             = map(string)
    # vpc_endpoint_apigw_prefix_id      = string
    # vpc_endpoint_dynamodb_prefix_id = string
    # vpc_subnet_ids                    = list(string)
    # vpc_id                            = string
    rest_api_execution_arn = string
    rest_api_arn           = string
  })

}

variable "client_manager_lambda_optional_iam_policy" {
  type    = bool
  default = true
}

variable "client_manager_lambda" {
  type = object({
    name                              = string
    filename                          = string
    cloudwatch_logs_retention_in_days = string
    environment_variables             = optional(map(string), {})
    table_client_registrations_arn    = optional(string, "")
    cognito_user_pool_arn             = optional(string, "")
    table_idp_internal_users_arn      = optional(string, "")
    table_idp_internal_users_gsi_arn  = optional(string, "")
    # TODO: move client_manager_lambda to VPC
    # vpc_endpoint_apigw_prefix_id      = string
    # vpc_endpoint_dynamodb_prefix_id   = string
    # vpc_subnet_ids                    = list(string)
    # vpc_id                            = string
  })
}

variable "rest_api_id" {
  type = string
}

variable "eventbridge_pipe_sessions" {
  type = object({
    pipe_name                     = string
    kms_sessions_table_alias      = string
    maximum_retry_attempts        = number
    maximum_record_age_in_seconds = number
  })
  default = null
}

variable "eventbridge_pipe_invalidate_cache" {
  type = object({
    pipe_name                     = string
    maximum_retry_attempts        = number
    maximum_record_age_in_seconds = number
  })
  default = null
}

variable "sns_topic_arn" {
  type = string
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
    sns_topic_alarm_arn = string
    scaling_policy      = optional(string, null)
  }))
}

variable "lambda_alarms" {
  type = map(object({
    metric_name         = string
    namespace           = string
    threshold           = number
    evaluation_periods  = number
    period              = number
    statistic           = string
    comparison_operator = string
    sns_topic_alarm_arn = string
    treat_missing_data  = string
  }))
}

variable "dlq_alarms" {
  type = object({
    metric_name         = string
    namespace           = string
    threshold           = number
    evaluation_periods  = number
    period              = number
    statistic           = string
    comparison_operator = string
    sns_topic_alarm_arn = string
  })
}

variable "client_alarm" {
  type = object({
    namespace = string
    clients = list(object({
      client_id     = string
      friendly_name = string
    }))
  })
  default = null
}

variable "idp_alarm" {
  type = object({
    namespace = string
    entity_id = list(string)
  })
  default = null
}
