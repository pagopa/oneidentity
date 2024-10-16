terraform {
  backend "s3" {
    bucket         = "terraform-state-1728293474"
    key            = "prod/eu-south-1/main/tfstate"
    region         = "eu-south-1"
    dynamodb_table = "terraform-lock"
  }
}
