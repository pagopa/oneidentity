variable "sqs_queue_name" {
  type        = string
  description = "SQS queue name for PDV failure calls"
}

variable "env_short" {
  type        = string
  description = "env short"
}

variable "sns_topic_arn" {
  type = string
}

variable "region_short" {
  type        = string
  description = "region short"
}