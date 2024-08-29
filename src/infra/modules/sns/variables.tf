variable "alarm_subscribers" {
  type        = string
  description = "SSM parameter store with the list alarm subscribers."
}

variable "sns_topic_name" {
  type        = string
  description = "SNS topic name."
}
