locals {
  project = format("%s-%s-%s", var.app_name, var.aws_region_short, var.env_short)
}
