terraform {
  backend "s3" {
    bucket         = "terraform-state-1785162310"
    key            = "uat-io/main/tfstate"
    region         = "eu-south-1"
    dynamodb_table = "terraform-lock"
  }
}
