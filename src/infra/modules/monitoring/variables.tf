variable "aws_region" {
  type = string
}

variable "api_name" {
  type = string
}

variable "dynamodb_table_name" {
  type = string
}

variable "ecs" {
  type = object({
    service_name = string,
    cluster_name = string
  })
}