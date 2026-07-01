variable "cache_name" {
  type        = string
  description = "Name of the ElastiCache Serverless cache."
}

variable "vpc_id" {
  type        = string
  description = "VPC identifier where cache security group is created."
}

variable "subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs used by ElastiCache Serverless."
}

variable "allowed_security_group_ids" {
  type        = list(string)
  description = "Security groups allowed to connect to Valkey on port 6379."

  validation {
    condition     = length(var.allowed_security_group_ids) > 0
    error_message = "At least one security group must be allowed to access Valkey."
  }
}

variable "major_engine_version" {
  type        = string
  description = "Valkey major engine version for ElastiCache Serverless."
  default     = "8"
}

variable "snapshot_retention_limit" {
  type        = number
  description = "Snapshot retention period in days."
  default     = 1
}

variable "daily_snapshot_time" {
  type        = string
  description = "Daily snapshot time in UTC format HH:MM."
  default     = "03:00"
}

variable "data_storage_maximum_gb" {
  type        = number
  description = "Maximum data storage in GB for the serverless cache."
  default     = 1

  validation {
    condition     = var.data_storage_maximum_gb <= 1
    error_message = "data_storage_maximum_gb must be 1 GB."
  }
}

variable "ecpu_per_second_maximum" {
  type        = number
  description = "Maximum ECPU per second for the serverless cache."
  default     = 1000

  validation {
    condition     = var.ecpu_per_second_maximum >= 1
    error_message = "ecpu_per_second_maximum must be greater than or equal to 1."
  }
}

variable "alarm_sns_topic_arn" {
  type        = string
  description = "SNS topic ARN used for cache alarm notifications."
  default     = null
}

variable "cache_alarms" {
  type = map(object({
    metric_name         = string
    threshold           = number
    comparison_operator = string
    evaluation_periods  = number
    period              = number
    statistic           = string
    treat_missing_data  = optional(string, "notBreaching")
  }))
  description = "CloudWatch alarms for Valkey serverless cache health and saturation."
  default = {
    throttled_cmds = {
      metric_name         = "ThrottledCmds"
      threshold           = 0
      comparison_operator = "GreaterThanThreshold"
      evaluation_periods  = 1
      period              = 300
      statistic           = "Sum"
      treat_missing_data  = "notBreaching"
    }
    evictions = {
      metric_name         = "Evictions"
      threshold           = 0
      comparison_operator = "GreaterThanThreshold"
      evaluation_periods  = 1
      period              = 300
      statistic           = "Sum"
      treat_missing_data  = "notBreaching"
    }
  }
}

variable "tags" {
  type        = map(string)
  description = "Tags applied to cache resources."
  default     = {}
}
