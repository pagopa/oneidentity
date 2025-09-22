variable "sqs_queue_name" {
  type        = string
  description = "SQS queue name for PDV failure calls"
}

variable "kms_ssm_enable_rotation" {
  type    = bool
  default = true
}

variable "kms_rotation_period_in_days" {
  type    = number
  default = 365
}