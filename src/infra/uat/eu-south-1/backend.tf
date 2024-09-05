terraform {
  backend "s3" {
    bucket         = "terraform-state-1723446137"
    key            = "uat/main/tfstate"
    region         = "eu-south-1"
    dynamodb_table = "terraform-lock"
  }
}
