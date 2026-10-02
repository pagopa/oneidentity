variable "alarm_subscribers" {
  type        = string
  description = "SSM parameter store with the list alarm subscribers."
}

variable "sns_topic_name" {
  type        = string
  description = "SNS topic name."
}

variable "account_id" {
  type        = string
  description = "AWS account ID."
}

variable "aws_region" {
  type        = string
  description = "AWS region."
}

variable "ecs_deployment_failed_rule_name" {
  type        = string
  description = "Name of the ECS deployment-failed EventBridge rule allowed to publish to the topic."
}