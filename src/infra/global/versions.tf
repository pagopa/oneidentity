terraform {
  required_version = "1.7.4"

  backend "s3" {}
  # backend "local" {}

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.43.0"
    }
  }
}