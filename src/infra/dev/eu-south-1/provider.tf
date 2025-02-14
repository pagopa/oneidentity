terraform {
  required_version = "1.7.4"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.74.0"
    }
    betteruptime = { 
      source  = "BetterStackHQ/better-uptime"
      version = ">= 0.9.3"
    }
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = var.tags
  }
}

provider "betteruptime" {

}
