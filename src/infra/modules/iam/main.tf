data "aws_iam_policy" "admin_access" {
  name = "AdministratorAccess"
}

data "aws_iam_policy" "read_access" {
  name = "ReadOnlyAccess"
}


data "aws_caller_identity" "current" {}

# github openid identity provider.
resource "aws_iam_openid_connect_provider" "github" {
  url = "https://token.actions.githubusercontent.com"

  client_id_list = [
    "sts.amazonaws.com",
  ]

  thumbprint_list = [
    "6938fd4d98bab03faadb97b34396831e3780aea1",
    "1c58a3a8518e8759bf075b76b750d4f2df264fcd",
  ]
}

resource "aws_iam_role" "githubiac" {
  name        = "${var.prefix}-github-iac-role"
  description = "Role to assume to create the infrastructure."


  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          "Federated" : "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/token.actions.githubusercontent.com"
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" : "sts.amazonaws.com",
            "token.actions.githubusercontent.com:sub" : "repo:${var.github_repository}:ref:refs/heads/main"
          }
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "githubiac" {
  role       = aws_iam_role.githubiac.name
  policy_arn = data.aws_iam_policy.admin_access.arn
}

resource "aws_iam_role" "githubiac_plan" {
  name        = "${var.prefix}-github-iac-role-plan"
  description = "Role to assume to create the infrastructure."


  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          "Federated" : "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/token.actions.githubusercontent.com"
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

resource "aws_iam_role_policy_attachment" "githubiac_plan" {
  role       = aws_iam_role.githubiac_plan.name
  policy_arn = data.aws_iam_policy.read_access.arn
}

resource "aws_iam_policy" "githubiac_plan_policy" {
  name        = "${var.prefix}-github-iac-policy-plan"
  description = "Policy to plan Infrastructure"

  policy = jsonencode({

    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:DeleteObject"
        ]
        Resource = "*"
        Condition = {
          StringEquals = { "aws:ResourceTag/Scope" : "tfstate" }
        }
      },
      {
        Effect = "Allow"
        Action = [
          "dynamodb:PutItem",
          "dynamodb:DeleteItem",
        ]
        Resource = "arn:aws:dynamodb:*:*:table/terraform*"
      },
      {
        Resource = "*",
        Action = [
          "kms:Decrypt"
        ],
        Effect = "Allow"
      }
    ]
  })

}



resource "aws_iam_role_policy_attachment" "githubiac_plan_policy" {
  role       = aws_iam_role.githubiac_plan.name
  policy_arn = aws_iam_policy.githubiac_plan_policy.arn
}

