## Github 

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


## client registration lambda

data "aws_iam_policy_document" "client_registration_lambda" {
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:Scan"
    ]
    resources = [
      var.client_registration_lambda.table_client_registrations_arn
    ]
  }
}

module "security_group_lambda_client_registration" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "4.17.2"

  name        = "${var.client_registration_lambda.name}-sg"
  description = "Security Group for Lambda Egress"

  vpc_id = var.client_registration_lambda.vpc_id

  egress_cidr_blocks      = []
  egress_ipv6_cidr_blocks = []

  # Prefix list ids to use in all egress rules in this module
  egress_prefix_list_ids = [var.metadata_lambda.vpc_endpoint_dynamodb_prefix_id]

  egress_rules = ["https-443-tcp"]
}

module "client_registration_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.client_registration_lambda.name
  description             = "Lambda function OIDC Dynamic Client Registration."
  runtime                 = "provided.al2023"
  handler                 = "not.used.in.provided.runtime"
  architectures           = ["x86_64"]
  create_package          = false
  local_existing_package  = var.client_registration_lambda.filename
  ignore_source_code_hash = true

  publish = true

  attach_policy_json    = true
  policy_json           = data.aws_iam_policy_document.client_registration_lambda.json
  attach_network_policy = true

  vpc_subnet_ids         = var.client_registration_lambda.vpc_subnet_ids
  vpc_security_group_ids = [module.security_group_lambda_client_registration.security_group_id]

  environment_variables = {
  }

  cloudwatch_logs_retention_in_days = var.client_registration_lambda.cloudwatch_logs_retention_in_days

  memory_size = 1024
  timeout     = 30

}

## Lambda metadata

data "aws_iam_policy_document" "metadata_lambda" {
  statement {
    effect    = "Allow"
    actions   = ["dynamodb:Scan"]
    resources = ["${var.table_client_registrations_arn}"]
  }

}

module "security_group_lambda_metadata" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "4.17.2"

  name        = "${var.metadata_lambda.name}-sg"
  description = "Security Group for Lambda Egress"

  vpc_id = var.metadata_lambda.vpc_id

  egress_cidr_blocks      = []
  egress_ipv6_cidr_blocks = []

  # Prefix list ids to use in all egress rules in this module
  egress_prefix_list_ids = [var.metadata_lambda.vpc_endpoint_dynamodb_prefix_id]

  egress_rules = ["https-443-tcp"]
}

module "metadata_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.metadata_lambda.name
  description             = "Lambda function metadata."
  runtime                 = "java21"
  handler                 = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
  create_package          = false
  local_existing_package  = var.metadata_lambda.filename
  ignore_source_code_hash = true

  publish = true

  attach_policy_json    = true
  policy_json           = data.aws_iam_policy_document.metadata_lambda.json
  attach_network_policy = true

  environment_variables  = var.metadata_lambda.environment_variables
  vpc_subnet_ids         = var.metadata_lambda.vpc_subnet_ids
  vpc_security_group_ids = [module.security_group_lambda_metadata.security_group_id]

  cloudwatch_logs_retention_in_days = var.metadata_lambda.cloudwatch_logs_retention_in_days

  memory_size = 512
  timeout     = 30
  snap_start  = true

}

## Lambda idp_metadata

data "aws_iam_policy_document" "idp_metadata_lambda" {
  statement {
    effect    = "Allow"
    actions   = ["s3:GetObject"]
    resources = ["${var.idp_metadata_lambda.s3_idp_metadata_bucket_arn}/*"]
  }

  statement {
    effect = "Allow"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:Query",
      "dynamodb:PutItem",
      "dynamodb:DeleteItem",
    ]
    resources = [
      "${var.dynamodb_table_idpMetadata.table_arn}",
      "${var.dynamodb_table_idpMetadata.gsi_pointer_arn}"
    ]
  }

}

module "security_group_lambda_idp_metadata" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "4.17.2"

  name        = "${var.idp_metadata_lambda.name}-sg"
  description = "Security Group for Lambda Egress"

  vpc_id = var.idp_metadata_lambda.vpc_id

  egress_cidr_blocks      = []
  egress_ipv6_cidr_blocks = []

  # Prefix list ids to use in all egress rules in this module
  egress_prefix_list_ids = [var.metadata_lambda.vpc_endpoint_dynamodb_prefix_id, var.idp_metadata_lambda.vpc_s3_prefix_id, ]

  egress_rules = ["https-443-tcp"]
}

module "idp_metadata_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.idp_metadata_lambda.name
  description             = "Lambda function idp metadata."
  runtime                 = "java21"
  handler                 = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
  create_package          = false
  local_existing_package  = var.idp_metadata_lambda.filename
  ignore_source_code_hash = true

  publish = true

  attach_policy_json    = true
  policy_json           = data.aws_iam_policy_document.idp_metadata_lambda.json
  attach_network_policy = true

  environment_variables  = var.idp_metadata_lambda.environment_variables
  vpc_subnet_ids         = var.idp_metadata_lambda.vpc_subnet_ids
  vpc_security_group_ids = [module.security_group_lambda_idp_metadata.security_group_id]

  cloudwatch_logs_retention_in_days = var.idp_metadata_lambda.cloudwatch_logs_retention_in_days

  allowed_triggers = {
    s3 = {
      principal  = "s3.amazonaws.com"
      source_arn = var.idp_metadata_lambda.s3_idp_metadata_bucket_arn
    }
  }

  memory_size = 512
  timeout     = 30
  snap_start  = true

}

resource "aws_s3_bucket_notification" "bucket_notification" {
  depends_on = [module.idp_metadata_lambda.lambda_function_name]
  bucket     = var.idp_metadata_lambda.s3_idp_metadata_bucket_id

  lambda_function {
    lambda_function_arn = module.idp_metadata_lambda.lambda_function_arn
    events              = ["s3:ObjectCreated:Put"]
  }
}


##Github Integration Lambda
module "is_gh_integration_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.is_gh_integration_lambda.name
  description             = "Lambda function is-gh integration."
  runtime                 = "java21"
  handler                 = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
  create_package          = false
  local_existing_package  = var.is_gh_integration_lambda.filename
  ignore_source_code_hash = true

  publish = true

  cloudwatch_logs_retention_in_days = var.is_gh_integration_lambda.cloudwatch_logs_retention_in_days

  allowed_triggers = [{
    sns = {
      principal  = "sns.amazonaws.com"
      source_arn = var.is_gh_integration_lambda.sns_topic_arn
    }
  }, null][var.is_gh_integration_lambda.sns_topic_arn != null ? 0 : 1]

  memory_size = 512
  timeout     = 30
  snap_start  = true

}

resource "aws_sns_topic_subscription" "is-gh-integration" {
  count      = var.is_gh_integration_lambda.sns_topic_arn != null ? 1 : 0
  topic_arn  = var.is_gh_integration_lambda.sns_topic_arn
  protocol   = "lambda"
  endpoint   = module.is_gh_integration_lambda.lambda_function_arn
  depends_on = [module.is_gh_integration_lambda.lambda_function_arn]
}

## Assertion Lambda ##
data "aws_iam_policy_document" "assertion_lambda" {
  statement {
    effect    = "Allow"
    actions   = ["s3:PutObject"]
    resources = ["${var.assertion_lambda.s3_assertion_bucket_arn}/*"]
  }

  statement {
    effect    = "Allow"
    actions   = ["kms:GenerateDataKey"]
    resources = [var.assertion_lambda.kms_assertion_key_arn]
  }
}

module "security_group_lambda_assertion" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "4.17.2"

  name        = "${var.assertion_lambda.name}-sg"
  description = "Security Group for Lambda Assertion"

  vpc_id = var.assertion_lambda.vpc_id

  egress_cidr_blocks      = []
  egress_ipv6_cidr_blocks = []

  # Prefix list ids to use in all egress rules in this module
  egress_prefix_list_ids = [
  var.assertion_lambda.vpc_s3_prefix_id, ]
  #ingress_prefix_list_ids = [var.assertion_lambda.vpc_endpoint_events_prefix_id]
  egress_rules = ["https-443-tcp"]
  #ingress_rules = ["https-443-tcp"]
}

resource "aws_sqs_queue" "dlq_lambda_assertion" {
  name = format("%s-dlq", var.assertion_lambda.name)
}

module "assertion_lambda" {
  source                 = "terraform-aws-modules/lambda/aws"
  version                = "7.4.0"
  function_name          = var.assertion_lambda.name
  description            = "Lambda function assertion."
  runtime                = "python3.12"
  handler                = "index.lambda_handler"
  create_package         = false
  local_existing_package = var.assertion_lambda.filename

  ignore_source_code_hash = true

  publish = true

  attach_policy_json = true
  policy_json        = data.aws_iam_policy_document.assertion_lambda.json

  environment_variables = var.assertion_lambda.environment_variables

  attach_network_policy = true

  vpc_subnet_ids         = var.assertion_lambda.vpc_subnet_ids
  vpc_security_group_ids = [module.security_group_lambda_assertion.security_group_id]

  ### DLQ ###
  attach_dead_letter_policy = true
  dead_letter_target_arn    = aws_sqs_queue.dlq_lambda_assertion.arn

  allowed_triggers = {
    events = {
      principal  = "events.amazonaws.com"
      source_arn = aws_pipes_pipe.sessions.arn
    }
  }

  memory_size = 512
  timeout     = 30

  cloudwatch_logs_retention_in_days = var.assertion_lambda.cloudwatch_logs_retention_in_days

}

resource "aws_cloudwatch_metric_alarm" "lambda_errors" {
  for_each = var.lambda_alarms
  alarm_name = lower(format("%s-%s-lambda-%s", each.key, each.value.metric_name,
  each.value.threshold))
  comparison_operator = each.value.comparison_operator
  evaluation_periods  = each.value.evaluation_periods
  metric_name         = each.value.metric_name
  namespace           = each.value.namespace
  period              = each.value.period
  statistic           = each.value.statistic
  threshold           = each.value.threshold
  treat_missing_data  = each.value.treat_missing_data

  dimensions = {
    FunctionName = each.key
  }

  alarm_actions = [each.value.sns_topic_alarm_arn]
}

resource "aws_cloudwatch_metric_alarm" "dlq_assertions" {
  alarm_name = format("%s-%s-Dlq-%s", module.assertion_lambda.lambda_function_name, var.dlq_alarms.metric_name,
  var.dlq_alarms.threshold)
  comparison_operator = var.dlq_alarms.comparison_operator
  evaluation_periods  = var.dlq_alarms.evaluation_periods
  metric_name         = var.dlq_alarms.metric_name
  namespace           = var.dlq_alarms.namespace
  period              = var.dlq_alarms.period
  statistic           = var.dlq_alarms.statistic
  threshold           = var.dlq_alarms.threshold


  dimensions = {
    QueueName = aws_sqs_queue.dlq_lambda_assertion.name
  }

  alarm_actions = [var.dlq_alarms.sns_topic_alarm_arn]
}

