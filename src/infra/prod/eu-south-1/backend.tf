terraform {
  backend "s3" {
    bucket         = "terraform-state-1716283937"
    key            = "peod/main/tfstate"
    region         = "eu-south-1"
    dynamodb_table = "terraform-lock"
  }
}