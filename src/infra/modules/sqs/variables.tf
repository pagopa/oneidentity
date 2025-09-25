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
