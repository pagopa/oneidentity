terraform {
  required_version = "1.7.4"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.74.0"
    }
  }
}

provider "aws" {
  region  = var.aws_region
  profile = "ppa-oneidentity-uat"
  default_tags {
    tags = var.tags
  }
}
