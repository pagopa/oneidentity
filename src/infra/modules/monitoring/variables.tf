variable "main_dashboard_name" {
  type        = string
  description = "Name of the main dashboard."
}

variable "api_methods_dashboard_name" {
  type        = string
  description = "Name of the api methods dashboard."
}

variable "aws_region" {
  type = string
}

variable "api_name" {
  type = string
}

variable "sessions_table" {
  type        = string
  description = "Dynamodb Sessions table"
}

variable "client_registrations_table" {
  type        = string
  description = "Dynamodb ClientRegistrations table"
}

variable "ecs" {
  type = object({
    service_name = string,
    cluster_name = string
  })
}

variable "nlb" {
  type = object({
    arn_suffix              = string
    target_group_arn_suffix = string
  })
  description = "Network load balancer configurations."
}
