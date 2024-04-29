variable "account_id" {
  type        = string
  description = "AWS Account id."
}


variable "ecr_registers" {
  type = list(object({
    name                     = string
    number_of_images_to_keep = number
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

variable "service_idp" {
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

    subnet_ids = list(string)

    load_balancer = object({
      target_group_arn  = string
      security_group_id = string
    })

  })
}

variable "github_repository" {
  type        = string
  description = "Github repository responsible to deploy ECS tasks in the form <organization|user/repository>."
}

variable "table_saml_responces_arn" {
  type        = string
  description = "Dynamodb table saml responses arn."

}
