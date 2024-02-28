variable "aws_region" {
  type        = string
  description = "AWS region to create resources. Default Milan"
  default     = "eu-south-1"
}

variable "app_name" {
  type        = string
  description = "App name."
  default     = "oneidentity"
}

variable "env_short" {
  type        = string
  default     = "d"
  description = "Evnironment short."
}

variable "vpc_cidr" {
  type        = string
  default     = "10.0.0.0/17"
  description = "VPC cidr."
}

variable "azs" {
  type        = list(string)
  description = "Availability zones"
  default     = ["eu-south-1a", "eu-south-1b", "eu-south-1c"]
}

variable "vpc_private_subnets_cidr" {
  type        = list(string)
  description = "Private subnets list of cidr."
}

variable "vpc_public_subnets_cidr" {
  type        = list(string)
  description = "Private subnets list of cidr."
}

variable "vpc_internal_subnets_cidr" {
  type        = list(string)
  description = "Internal subnets list of cidr. Mainly for private endpoints"
}

variable "enable_nat_gateway" {
  type        = bool
  description = "Enable/Create nat gateway"
  default     = false
}

variable "single_nat_gateway" {
  type        = bool
  description = "Create just one natgateway"
  default     = false

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

variable "tags" {
  type = map(any)
  default = {
    CreatedBy = "Terraform"
  }
}
