variable "account_id" {
  type        = string
  description = "AWS Account id."
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
      name          = string
      cpu           = number
      memory        = number
      image_name    = string
      image_version = string
      containerPort = number
      hostPort      = number
    })
    autoscaling = object({
      enable       = bool
      min_capacity = number
      max_capacity = number
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
    name                           = string
    filename                       = string
    table_client_registrations_arn = string
  })

}

variable "metadata_lambda" {
  type = object({
    name                           = string
    filename                       = string
    table_client_registrations_arn = string
    environment_variables          = map(string)
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