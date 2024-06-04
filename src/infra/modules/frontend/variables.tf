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

## API Gateway ## 

variable "rest_api_name" {
  type = string
  description = "Rest api name"
  
}

variable "rest_api_stage" {
  type = string 
  description = "Rest api stage name"
  default = "v1"  
}

variable "api_gateway_target_arns" {
  type = list(string)
  description = "List of target arn for the api gateway."
}

variable "nlb_dns_name" {
  type = string 
  description = "NLB dns name."
}