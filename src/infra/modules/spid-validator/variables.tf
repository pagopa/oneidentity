variable "aws_region" {
  type        = string
  description = "AWS region."

}

variable "ecr_repository_name" {
  type        = string
  description = "ECR repository name for spid validator."
}

variable "repository_image_tag_mutability" {
  type        = string
  description = "The tag mutability setting for the repository. Must be one of: MUTABLE or IMMUTABLE. Defaults to IMMUTABLE"
  default     = "MUTABLE"
}

variable "spid_validator" {
  type = object({
    cluster_arn  = string
    service_name = string
    cpu          = optional(number, 512)
    memory       = optional(number, 1024)
    container = object({
      name                = string
      image_name          = string
      image_version       = string
      cpu                 = optional(number, 512)
      memory              = optional(number, 1024)
      logs_retention_days = optional(number, 14)
    })
  })

  description = "Spid validator configurations. When null the resources won't be created."

  default = null
}

variable "zone_id" {
  type        = string
  description = "Zone id where to create the validation record for the ACM certificates."
}

variable "zone_name" {
  type        = string
  description = "R53 zone name."
}

variable "alb_spid_validator_name" {
  type        = string
  description = "Alb name."
}

variable "vpc_id" {
  type        = string
  description = "VPC id"
}

variable "public_subnet_ids" {
  type        = list(string)
  description = "Public subnet ids."
}

variable "vpc_cidr_block" {
  type        = string
  description = "VPC cids block."
}

variable "private_subnets_ids" {
  type        = list(string)
  description = "Private subnets id"
}