terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">=5.49"
    }
  }
}

provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"
}