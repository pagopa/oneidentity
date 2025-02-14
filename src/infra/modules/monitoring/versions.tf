terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">=5.49"
    }

    betteruptime = { 
      source  = "BetterStackHQ/better-uptime"
      version = ">= 0.9.3"
    }
  }
}