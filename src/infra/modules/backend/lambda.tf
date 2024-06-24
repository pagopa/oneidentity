## client registration lambda

data "aws_iam_policy_document" "client_registration_lambda" {
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:GetItem"
    ]
    resources = [
      var.client_registration_lambda.table_client_registrations_arn
    ]
  }
}


module "client_registration_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.client_registration_lambda.name
  description             = "Lambda function to download client configuration files."
  runtime                 = "java17"
  handler                 = "example.HelloWorld::handleRequest"
  create_package          = false
  local_existing_package  = var.client_registration_lambda.filename
  ignore_source_code_hash = false

  publish = true

  #attach_policy_json = true
  #policy_json        = data.aws_iam_policy_document.lambda_webhook2.json

  environment_variables = {
  }

  memory_size = 128
  timeout     = 30

}

resource "aws_iam_role" "github_lambda_deploy" {
  name        = "GitHubDeployLambda"
  description = "Role to deploy lambda functions with github actions."


  assume_role_policy = local.assume_role_policy_github
}

resource "aws_iam_policy" "deploy_lambda" {
  name        = "DeployLambda"
  description = "Policy to deploy Lambda functions"

  policy = jsonencode({

    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "lambda:CreateFunction",
          "lambda:UpdateFunctionCode",
          "lambda:UpdateFunctionConfiguration"
        ]
        Resource = "*"
      }
    ]
  })

}

resource "aws_iam_role_policy_attachment" "deploy_lambda" {
  role       = aws_iam_role.github_lambda_deploy.name
  policy_arn = aws_iam_policy.deploy_lambda.id

}

## Deploy with github action
resource "aws_iam_role" "githubecsdeploy" {
  name        = format("%s-deploy", var.service_core.service_name)
  description = "Role to assume to deploy ECS tasks"


  assume_role_policy = local.assume_role_policy_github
}