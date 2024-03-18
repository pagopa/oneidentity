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
    vpc_endpoint_ids = list(string)
  })
  default = {
    types            = ["REGIONAL"]
    vpc_endpoint_ids = []
  }
}


variable "stage_name" {
  type        = string
  description = "Stage name."
}