locals {
  project = format("%s-%s-%s", var.app_name, var.aws_region_short, var.env_short)

  container_name      = "oneidentity"
  alb_name            = format("%s-alb", local.project)

  ecr_core = format("%s-core-ecr", local.project)

}
