variable "aws_region" {
  type        = string
  description = "AWS region to create resources. Default Milan"
  default     = "eu-south-1"
}

variable "aws_region_short" {
  type        = string
  description = "AWS region short format."
  default     = "es-1"
}

variable "app_name" {
  type        = string
  description = "App name."
  default     = "oneid"
}

variable "env_short" {
  type        = string
  default     = "d"
  description = "Evnironment short."
}

## R53 DNS zone ##
variable "r53_dns_zone" {
  type = object({
    name    = string
    comment = string
  })
}

## ECS Cluster ##
variable "ecr_keep_images" {
  type        = number
  description = "Number of images to keep."
  default     = 3
}

variable "ecs_enable_container_insights" {
  type        = bool
  description = "Enable ecs cluster container inight."
  default     = false
}

variable "ecs_autoscaling_idp" {
  type = object({
    enable       = bool
    min_capacity = number
    max_capacity = number
  })
}

variable "idp_image_version" {
  type        = string
  description = "Image version idp."
}

## Storage S3 ## 
variable "assertion_bucket" {
  type = object({
    mfa_delete               = bool
    gracier_transaction_days = number
    expiration_days          = number
  })
  default = {
    mfa_delete               = false
    gracier_transaction_days = 90
    expiration_days          = 100
  }

}

## Database ##
variable "table_saml_responses_point_in_time_recovery_enabled" {
  type        = bool
  description = "Enable point in time recovery table saml responses"
  default     = false
}



# DNS
variable "dns_record_ttl" {
  type        = number
  description = "Dns record ttl (in sec)"
  default     = 86400 # 24 hours
}


variable "tags" {
  type = map(any)
  default = {
    CreatedBy = "Terraform"
  }
}
