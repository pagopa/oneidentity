terraform {
  backend "s3" {
    bucket         = "terraform-state-1785162376"
    key            = "prod-io/eu-south-1/main/tfstate"
    region         = "eu-south-1"
    dynamodb_table = "terraform-lock"
  }
}
