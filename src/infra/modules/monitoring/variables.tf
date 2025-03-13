variable "env_short" {
  type        = string
  description = "Name of the main dashboard."
}

variable "main_dashboard_name" {
  type        = string
  description = "Name of the main dashboard."
}

variable "api_methods_dashboard_name" {
  type        = string
  description = "Name of the api methods dashboard."
}

variable "detailed_metrics_dashboard_name" {
  type        = string
  description = "Name of the detailed metrics dashboard."
}

variable "idp_entity_ids" {
  type    = list(string)
  default = []
}

variable "clients" {
  type = list(object({
    client_id     = string
    friendly_name = string
  }))
  default = []
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
    service_name   = string,
    cluster_name   = string
    log_group_name = string
  })
}

variable "lambda_metadata" {
  type = object({
    log_group_name = string
  })
}

variable "lambda_client_registration" {
  type = object({
    log_group_name = string
  })
}

variable "nlb" {
  type = object({
    arn_suffix              = string
    target_group_arn_suffix = string
  })
  description = "Network load balancer configurations."
}

variable "query_files" {
  type    = list(string)
  default = [] # Empty list, we'll populate it with locals
}

variable "alarm_subscribers" {
  type        = string
  description = "SSM parameter store with the list alarm subscribers."
}

variable "ce_daily_budget" {
  type        = string
  description = "Cost Explorer daily budget."
  default     = "300"
}


variable "create_ce_budget" {
  type        = bool
  description = "Create Cost Explorer budget."
  default     = false

}