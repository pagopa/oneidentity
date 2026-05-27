locals {
  assume_role_policy_github = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Federated = "arn:aws:iam::${var.account_id}:oidc-provider/token.actions.githubusercontent.com"
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringLike = {
            "token.actions.githubusercontent.com:sub" : [
              "repo:${var.github_repository}:*"
            ]
          },
          "ForAllValues:StringEquals" = {
            "token.actions.githubusercontent.com:iss" : "https://token.actions.githubusercontent.com",
            "token.actions.githubusercontent.com:aud" : "sts.amazonaws.com"
          }
        }
      }
    ]
  })

  bucket_lambda_code              = format("lambda-code-%s", random_integer.bucket_lambda_code_suffix.result)
  metrics_archiver_lambda_config  = var.metrics_archiver_enabled ? var.metrics_archiver_lambda : null
  metrics_archiver_lambda_map     = local.metrics_archiver_lambda_config == null ? {} : { metrics_archiver = local.metrics_archiver_lambda_config }
  metrics_archiver_lambda_vpc_map = try(local.metrics_archiver_lambda_config.vpc_id, null) == null ? {} : local.metrics_archiver_lambda_map
}
