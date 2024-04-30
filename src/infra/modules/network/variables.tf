variable "vpc_name" {
  type        = string
  description = "Name of the vpc"
}

variable "vpc_cidr" {
  type        = string
  description = "VPC cidr."
}

variable "azs" {
  type        = list(string)
  description = "Availability zones"
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