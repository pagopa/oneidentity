variable "account_id" {
  type        = string
  description = "AWS Account id."
}

variable "aws_region" {
  type        = string
  description = "AWS Region."
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

#TODO fix name
variable "ssm_cert_key" {
  type = object({
    cert_pem = optional(string, "cert.pem")
    key_pem  = optional(string, "key.pem")
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

variable "table_client_registrations_arn" {
  type        = string
  description = "Dynamodb table client registrations arn."
}

variable "kms_sessions_table_alias_arn" {
  type        = string
  description = "Kms key used to encrypt and dectypt session table."
}

variable "client_registration_lambda" {
  type = object({
    name                              = string
    filename                          = string
    table_client_registrations_arn    = string
    cloudwatch_logs_retention_in_days = number
    vpc_id                            = string
    vpc_endpoint_dynamodb_prefix_id   = string
    vpc_subnet_ids                    = list(string)

  })

}

variable "metadata_lambda" {
  type = object({
    name                              = string
    filename                          = string
    table_client_registrations_arn    = string
    environment_variables             = map(string)
    vpc_id                            = string
    vpc_subnet_ids                    = list(string)
    vpc_endpoint_dynamodb_prefix_id   = string
    vpc_endpoint_ssm_prefix_id        = string
    cloudwatch_logs_retention_in_days = number
  })

}

## Network load balancer ##
variable "nlb_name" {
  type        = string
  description = "Network load balancer name"
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

variable "assertion_lambda" {
  type = object({
    name                              = string
    filename                          = string
    s3_assertion_bucket_arn           = string
    kms_assertion_key_arn             = string
    environment_variables             = map(string)
    cloudwatch_logs_retention_in_days = number
    vpc_s3_prefix_id                  = string
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
  })

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
