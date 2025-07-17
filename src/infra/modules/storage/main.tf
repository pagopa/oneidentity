resource "random_integer" "asset_bucket_suffix" {
  min = 1000
  max = 9999
}
resource "random_integer" "asset_bucket_control_panel_suffix" {
  min = 1000
  max = 9999
}

resource "random_integer" "asset_bucket_internal_idp_suffix" {
  min = 1000
  max = 9999
}

resource "random_integer" "idp_metadata_bucket_suffix" {
  min = 1000
  max = 9999
}

module "s3_assets_bucket" {

  count = var.create_assets_bucket ? 1 : 0

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

module "s3_assets_control_panel_bucket" {

  count = var.create_assets_control_panel_bucket ? 1 : 0

  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.assets_control_panel_bucket
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  tags = {
    Name = local.assets_control_panel_bucket
  }
}

module "s3_internal_idp_assets_bucket" {

  count = var.create_assets_internal_idp_bucket ? 1 : 0

  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.assets_internal_idp_bucket
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  tags = {
    Name = local.assets_internal_idp_bucket
  }
}

module "s3_idp_metadata_bucket" {
  count   = var.create_idp_metadata_bucket ? 1 : 0
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
  count      = var.create_assets_bucket ? 1 : 0
  role       = aws_iam_role.githubS3deploy.name
  policy_arn = aws_iam_policy.github_s3_policy[0].arn
}

resource "aws_iam_role_policy_attachment" "deploy_cp_s3" {
  count      = var.create_assets_control_panel_bucket ? 1 : 0
  role       = aws_iam_role.githubS3deploy.name
  policy_arn = aws_iam_policy.github_s3_policy[0].arn
}

resource "aws_iam_role_policy_attachment" "deploy_internal_idp_s3" {
  count      = var.create_assets_internal_idp_bucket ? 1 : 0
  role       = aws_iam_role.githubS3deploy.name
  policy_arn = aws_iam_policy.github_s3_internal_idp_policy[0].arn
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
  count       = var.create_assets_bucket ? 1 : 0
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
          module.s3_assets_bucket[0].s3_bucket_arn,
          "${module.s3_assets_bucket[0].s3_bucket_arn}/*",
          module.s3_assets_control_panel_bucket[0].s3_bucket_arn
        ]
      }
    ]
  })
}

resource "aws_iam_policy" "github_s3_internal_idp_policy" {
  count       = var.create_assets_internal_idp_bucket ? 1 : 0
  name        = "${var.role_prefix}-deploy-assets-internal-idp"
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
          "${module.s3_assets_control_panel_bucket[0].s3_bucket_arn}/*",
          module.s3_internal_idp_assets_bucket[0].s3_bucket_arn,
          "${module.s3_internal_idp_assets_bucket[0].s3_bucket_arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "upload_idp" {
  count      = var.create_idp_metadata_bucket ? 1 : 0
  role       = aws_iam_role.upload_idp_role.name
  policy_arn = aws_iam_policy.upload_idp_policy[0].arn
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
  count       = var.create_idp_metadata_bucket ? 1 : 0
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
          module.s3_idp_metadata_bucket[0].s3_bucket_arn,
          "${module.s3_idp_metadata_bucket[0].s3_bucket_arn}/*"
        ]
      }
    ]
  })
}
