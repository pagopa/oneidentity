variable "prefix" {
  type    = string
  default = "Prefix to assign to the resources."
}

variable "github_repository" {
  type        = string
  description = "Github federation repository"
}

variable "github_environment" {
  type        = string
  description = "GitHub environment allowed to assume the infrastructure apply role."
  default     = null
}
