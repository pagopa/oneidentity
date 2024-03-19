variable "name" {
  type        = string
  description = "Rest api name"
}


variable "body" {
  type        = string
  description = "Open api json body"
}

variable "endpoint_configuration" {
  type = object({
    types            = list(string)
    vpc_endpoint_ids = optional(list(string), null)
  })
}

variable "stage_name" {
  type        = string
  description = "Stage name."
}


variable "custom_domain_name" {
  type        = string
  description = "Api gateway custom domain name."
  default     = null
}

variable "certificate_arn" {
  type        = string
  description = "Api Gateway certificate arn"
  default     = null
}

variable "api_mapping_key" {
  type        = string
  description = "The API mapping key."
  default     = null
}