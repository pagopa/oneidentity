# Cognito user pool 
resource "aws_cognito_user_pool" "main" {
  name = var.cognito.user_pool_name

  schema {
    attribute_data_type = "String"
    name                = "email"
    required            = true
  }

  username_attributes = ["email"]

  password_policy {
    minimum_length                   = 8
    require_lowercase                = true
    require_numbers                  = true
    require_symbols                  = true
    require_uppercase                = true
    temporary_password_validity_days = 7
  }

  account_recovery_setting {
    recovery_mechanism {
      name     = "admin_only"
      priority = 1
    }
  }

  admin_create_user_config {
    allow_admin_create_user_only = false
  }

  /*
  schema {
    attribute_data_type      = "String"
    developer_only_attribute = true
    mutable                  = false
    name                     = "clientID"
    required                 = false #to check
  }
  */

  lambda_config {
    pre_sign_up = module.cognito_presignup_lambda.lambda_function_arn
  }
  deletion_protection = "INACTIVE"

  email_configuration {
    email_sending_account = "COGNITO_DEFAULT"
  }

  verification_message_template {
    default_email_option = "CONFIRM_WITH_CODE"
    email_message        = "Your verification code is {####}"
  }
}

resource "aws_cognito_user_pool_domain" "main" {
  domain       = var.cognito.user_pool_domain
  user_pool_id = aws_cognito_user_pool.main.id
}


resource "aws_cognito_user_pool_client" "client" {
  name         = var.cognito.user_pool_client
  user_pool_id = aws_cognito_user_pool.main.id

  allowed_oauth_flows_user_pool_client = true

  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH",
    "ALLOW_USER_SRP_AUTH",
  ]


  allowed_oauth_flows  = ["code", "implicit"]
  allowed_oauth_scopes = ["email", "openid"]

  callback_urls = ["${var.cognito.callback_url}"]
  logout_urls   = ["${var.cognito.logout_url}"] # Update with your app's logout URL

  supported_identity_providers = ["COGNITO"]


}

module "cognito_presignup_lambda" {
  source                 = "terraform-aws-modules/lambda/aws"
  version                = "7.4.0"
  function_name          = var.cognito_presignup_lambda.name
  description            = "Lambda function cognito preSignUp."
  runtime                = "python3.12"
  handler                = "index.lambda_handler"
  create_package         = false
  local_existing_package = var.cognito_presignup_lambda.filename

  ignore_source_code_hash = true

  publish = true

  allowed_triggers = {
    events = {
      principal  = "cognito-idp.amazonaws.com"
      source_arn = aws_cognito_user_pool.main.arn
    }
  }

  memory_size = 512
  timeout     = 30

  cloudwatch_logs_retention_in_days = var.cognito_presignup_lambda.cloudwatch_logs_retention_in_days

}

