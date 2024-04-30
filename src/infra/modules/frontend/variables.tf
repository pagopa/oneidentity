variable "alb_name" {
  type        = string
  description = "Load balancer name."
}

variable "vpc_id" {
  type        = string
  description = "VPC id."
}


variable "public_subnet_ids" {
  type        = list(string)
  description = "Public subnet ids."
}

variable "acm_certificate_arn" {
  type        = string
  description = "Acm certificate arn."
}

## DNS ##
variable "r53_dns_zones" {
  type        = any
  description = "R53 DNS Zones."
}
