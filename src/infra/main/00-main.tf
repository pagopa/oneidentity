terraform {
  required_version = "1.7.4"

  backend "s3" {}
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.38.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = var.tags
  }
}

locals {
  project = format("%s-%s-%s", var.app_name, var.aws_region, var.env_short)
}

# data "aws_caller_identity" "current" {}