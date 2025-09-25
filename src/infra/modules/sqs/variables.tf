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
