locals {
  project = format("%s-%s-%s", var.app_name, var.aws_region_short, var.env_short)

  container_poc1_port = 8080
  container_poc2_port = 8080
  container_name      = "oneidentity"
  alb_name            = format("%s-alb", local.project)
}