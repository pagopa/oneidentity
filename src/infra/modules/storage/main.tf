resource "random_integer" "asset_bucket_suffix" {
  min = 1000
  max = 9999
}


resource "random_integer" "idp_metadata_bucket_suffix" {
  min = 1000
  max = 9999
}

module "s3_assets_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.assets_bucket
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  tags = {
    Name = local.assets_bucket
  }
}

module "s3_idp_metadata_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.idp_metadata_bucket
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  tags = {
    Name = local.idp_metadata_bucket
  }
}

resource "aws_iam_role_policy_attachment" "deploy_s3" {
  role       = aws_iam_role.githubS3deploy.name
  policy_arn = aws_iam_policy.github_s3_policy.arn
}

resource "aws_iam_role" "githubS3deploy" {
  name        = "${var.role_prefix}-deploy-assets"
  description = "Role to assume to copy to S3."


  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          "Federated" : "arn:aws:iam::${var.account_id}:oidc-provider/token.actions.githubusercontent.com"
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringLike = {
            "token.actions.githubusercontent.com:sub" : "repo:${var.github_repository}:*"
          },
          "ForAllValues:StringEquals" = {
            "token.actions.githubusercontent.com:iss" : "https://token.actions.githubusercontent.com",
            "token.actions.githubusercontent.com:aud" : "sts.amazonaws.com"
          }
        }
      }
    ]
  })
}

resource "aws_iam_policy" "github_s3_policy" {
  name        = "${var.role_prefix}-deploy-assets"
  description = "Policy to deploy to S3"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "s3:ListBucket",
          "s3:GetObject",
          "s3:PutObject"
        ],
        Resource = [
          module.s3_assets_bucket.s3_bucket_arn,
          "${module.s3_assets_bucket.s3_bucket_arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "upload_idp" {
  role       = aws_iam_role.upload_idp_role.name
  policy_arn = aws_iam_policy.upload_idp_policy.arn
}

resource "aws_iam_role" "upload_idp_role" {
  name        = "${var.role_prefix}-upload-idp"
  description = "Role to assume to copy to S3."


  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          "Federated" : "arn:aws:iam::${var.account_id}:oidc-provider/token.actions.githubusercontent.com"
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringLike = {
            "token.actions.githubusercontent.com:sub" : "repo:${var.github_repository}:*"
          },
          "ForAllValues:StringEquals" = {
            "token.actions.githubusercontent.com:iss" : "https://token.actions.githubusercontent.com",
            "token.actions.githubusercontent.com:aud" : "sts.amazonaws.com"
          }
        }
      }
    ]
  })
}

resource "aws_iam_policy" "upload_idp_policy" {
  name        = "${var.role_prefix}-upload-idp"
  description = "Policy to deploy to S3"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "s3:ListBucket",
          "s3:GetObject",
          "s3:PutObject"
        ],
        Resource = [
          module.s3_idp_metadata_bucket.s3_bucket_arn,
          "${module.s3_idp_metadata_bucket.s3_bucket_arn}/*"
        ]
      }
    ]
  })
}