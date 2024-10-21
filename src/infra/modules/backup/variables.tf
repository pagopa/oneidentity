variable "prefix" {
  type    = string
  default = "Prefix to assign to the resources."
}

variable "backup_name" {
  type        = string
  description = "Backup name. This name will be use to assign a name to the vault and the plane"
}

variable "backup_rule" {
  type        = any
  description = "A rule object that specifies a scheduled task that is used to back up a selection of resources"
}