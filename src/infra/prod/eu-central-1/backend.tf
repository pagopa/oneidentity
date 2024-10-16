terraform {
  backend "s3" {
    bucket         = "terraform-state-1716283937"
    key            = "prod/eu-central-1/main/tfstate"
    region         = "eu-south-1"
    dynamodb_table = "terraform-lock"
  }
}
