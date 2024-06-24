variable "vpc_id" {
  type        = string
  description = "VPC id."
}


variable "public_subnet_ids" {
  type        = list(string)
  description = "Public subnet ids."
}

## DNS ##
variable "r53_dns_zones" {
  type        = any
  description = "R53 DNS Zones."
}

## API Gateway ## 

variable "rest_api_name" {
  type        = string
  description = "Rest api name"

}

variable "rest_api_stage" {
  type        = string
  description = "Rest api stage name"
  default     = "v1"
}

variable "api_gateway_target_arns" {
  type        = list(string)
  description = "List of target arn for the api gateway."
}

variable "api_gateway_plan" {
  type = object({
    name                 = string
    throttle_burst_limit = number
    throttle_rate_limit  = number
  })
  description = "Name of the plan associated to the set of apis."
}

variable "nlb_dns_name" {
  type        = string
  description = "NLB dns name."
}

variable "metadata_lamba_name" {
  type = string
  description = "lambda metadata arn"
}