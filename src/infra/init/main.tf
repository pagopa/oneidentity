terraform {
  required_version = "1.7.4"

  backend "s3" {}
  # backend "local" {}

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.43.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = var.tags
  }
}

# terraform state file setup
# create an S3 bucket to store the state file in

resource "aws_s3_bucket" "terraform_states" {
  bucket_prefix = "terraform-backend-"

  lifecycle {
    prevent_destroy = true
  }

  tags = {
    Name = "terraform-remote-state"
  }
}

resource "aws_s3_bucket_ownership_controls" "terraform_states" {
  bucket = aws_s3_bucket.terraform_states.id
  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_acl" "terraform_states" {
  bucket     = aws_s3_bucket.terraform_states.id
  acl        = "private"
  depends_on = [aws_s3_bucket_ownership_controls.terraform_states]
}

resource "aws_s3_bucket_public_access_block" "terraform_states" {
  bucket                  = aws_s3_bucket.terraform_states.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_versioning" "terraform_states" {
  bucket = aws_s3_bucket.terraform_states.id
  versioning_configuration {
    status = "Enabled"
  }
}

# create a DynamoDB table for locking the state file
resource "aws_dynamodb_table" "dynamodb-terraform-state-lock" {
  name           = format("terraform-lock-%s-%s", var.aws_region, var.environment)
  hash_key       = "LockID"
  read_capacity  = 2
  write_capacity = 2

  attribute {
    name = "LockID"
    type = "S"
  }
}
