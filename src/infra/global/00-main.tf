provider "aws" {
  region = var.aws_region
  default_tags {
    tags = var.tags
  }
}

locals {
  project = format("%s-%s", var.app_name, var.env_short)
}

data "aws_caller_identity" "current" {}

module "github" {
  source = "../modules/github"

  github_repository = "pagopa/oneidentity"
}