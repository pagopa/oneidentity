terraform {
  backend "s3" {
    bucket         = "terraform-backend-20240226144445189900000001"
    key            = "dev/main/tfstate"
    region         = "eu-south-1"
    dynamodb_table = "terraform-lock-eu-south-1-Dev"
  }
}