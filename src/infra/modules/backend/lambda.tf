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

resource "aws_iam_role" "githubecsdeploy_internal_idp" {
  count       = var.internal_idp_enabled ? 1 : 0
  name        = format("%s-deploy", var.service_internal_idp.service_name)
  description = "Role to assume to deploy ECS internal IDP tasks"


  assume_role_policy = local.assume_role_policy_github
}

resource "aws_iam_role" "github_lambda_deploy" {
  name               = format("%s-deploy-lambda", var.role_prefix)
  description        = "Role to deploy lambda functions with github actions."
  assume_role_policy = local.assume_role_policy_github
}

resource "aws_iam_policy" "deploy_lambda" {
  name        = format("%s-deploy-lambda", var.role_prefix)
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
      },
      {
        Action = [
          "s3:PutObject",
          "s3:GetObject"
        ]
        Effect = "Allow"
        Resource = [
          "${module.s3_lambda_code_bucket.s3_bucket_arn}/*"
        ]
      },
    ]
  })
}

resource "random_integer" "bucket_lambda_code_suffix" {
  min = 1000
  max = 9999
}

module "s3_lambda_code_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.bucket_lambda_code
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  tags = {
    Name = local.bucket_lambda_code
  }
}


## client registration lambda

data "aws_iam_policy_document" "client_registration_lambda" {
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem",
      "dynamodb:Scan"
    ]
    resources = [
      var.client_registration_lambda.table_client_registrations_arn
    ]
  }
  statement {
    effect  = "Allow"
    actions = ["sns:Publish"]
    resources = [
      var.sns_topic_arn
    ]
  }
  statement {
    sid    = "SSMGetCertParameters"
    effect = "Allow"
    actions = [
      "ssm:Describe*",
      "ssm:Get*",
      "ssm:List*"
    ]
    resources = [
      data.aws_ssm_parameter.certificate.arn,
      aws_ssm_parameter.key_pem.arn,
      "arn:aws:ssm:${var.aws_region}:${var.account_id}:parameter/apikey-pdv/plan-details"
    ]
  }

  statement {
    sid    = "SSMWriteParameters"
    effect = "Allow"
    actions = [
      "ssm:GetParameter",
      "ssm:GetParameters",
      "ssm:GetParameterHistory",
      "ssm:GetParametersByPath",
      "ssm:PutParameter",
      "ssm:DeleteParameter",
      "ssm:DeleteParameters",
      "ssm:AddTagsToResource",
      "ssm:RemoveTagsFromResource",
      "ssm:LabelParameterVersion"
    ]
    resources = [
      "arn:aws:ssm:${var.aws_region}:${var.account_id}:parameter/pdv/*"
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
  egress_prefix_list_ids = [
    var.client_registration_lambda.vpc_endpoint_dynamodb_prefix_id
  ]

  egress_rules = ["https-443-tcp"]
}

resource "aws_vpc_security_group_egress_rule" "client_registration_sec_group_egress_rule" {
  security_group_id            = module.security_group_lambda_client_registration.security_group_id
  from_port                    = 443
  ip_protocol                  = "tcp"
  to_port                      = 443
  referenced_security_group_id = var.client_registration_lambda.vpc_tls_security_group_endpoint_id
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

  environment_variables = var.client_registration_lambda.environment_variables

  cloudwatch_logs_retention_in_days = var.client_registration_lambda.cloudwatch_logs_retention_in_days

  memory_size = 1024
  timeout     = 30

}

## Lambda metadata

data "aws_iam_policy_document" "metadata_lambda" {
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:DescribeStream",
      "dynamodb:GetRecords",
      "dynamodb:GetShardIterator",
      "dynamodb:ListStreams",
    ]
    resources = [var.dynamodb_clients_table_stream_arn]
  }
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:Scan",
    ]
    resources = [var.table_client_registrations_arn]
  }
  statement {
    effect = "Allow"
    actions = [
      "kms:Decrypt",
      "kms:Encrypt",
    ]
    resources = [module.kms_key_pem.aliases["keyPem/SSM"].target_key_arn]
  }
  statement {
    effect = "Allow"
    actions = [
      "ssm:Describe*",
      "ssm:Get*",
      "ssm:List*"
    ]
    resources = [data.aws_ssm_parameter.certificate.arn, aws_ssm_parameter.key_pem.arn]
  }
  statement {
    effect = "Allow"
    actions = [
      "s3:PutObject",
      "s3:GetObject"
    ]
    resources = ["${var.metadata_lambda.assets_bucket_arn}/*"]
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
  egress_prefix_list_ids = [
    var.metadata_lambda.vpc_endpoint_dynamodb_prefix_id
  ]

  # egress_rules = ["https-443-tcp"]
}

resource "aws_security_group_rule" "metadata_vpc_tls" {
  type                     = "egress"
  from_port                = 443
  to_port                  = 443
  protocol                 = "tcp"
  security_group_id        = module.security_group_lambda_metadata.security_group_id
  source_security_group_id = var.metadata_lambda.vpc_endpoint_ssm_nsg_ids[1]
  prefix_list_ids          = [var.metadata_lambda.vpc_s3_prefix_id]

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

  allowed_triggers = {
    dynamodb = {
      principal  = "dynamodb.amazonaws.com"
      source_arn = var.dynamodb_clients_table_stream_arn
    }
  }

  environment_variables  = var.metadata_lambda.environment_variables
  vpc_subnet_ids         = var.metadata_lambda.vpc_subnet_ids
  vpc_security_group_ids = [module.security_group_lambda_metadata.security_group_id]

  cloudwatch_logs_retention_in_days = var.metadata_lambda.cloudwatch_logs_retention_in_days

  memory_size = 512
  timeout     = 30
  snap_start  = true

}

resource "aws_lambda_event_source_mapping" "trigger" {
  count = var.lambda_client_registration_trigger_enabled ? 1 : 0
  depends_on = [
    module.metadata_lambda.lambda_function_name,
    var.table_client_registrations_arn
  ]
  event_source_arn  = var.dynamodb_clients_table_stream_arn
  function_name     = module.metadata_lambda.lambda_function_arn
  starting_position = "LATEST"
  enabled           = true
}

resource "aws_cloudwatch_event_rule" "cert_key_changes" {
  name        = "capture-cert-key-change"
  description = "Capture each cert.pem and key.pem changes"

  event_pattern = jsonencode(
    {
      "source" : [
        "aws.ssm"
      ],
      "detail-type" : [
        "Parameter Store Change"
      ],
      "detail" : {
        "name" : [
          "cert.pem",
          "key.pem"
        ],
        "operation" : [
          "Create",
          "Update",
          "Delete",
          "LabelParameterVersion"
        ]
      }
  })
}

resource "aws_cloudwatch_event_target" "metadata_lambda" {
  rule = aws_cloudwatch_event_rule.cert_key_changes.name
  arn  = module.metadata_lambda.lambda_function_arn
}

resource "aws_lambda_permission" "cert_key_changes" {
  action        = "lambda:InvokeFunction"
  function_name = module.metadata_lambda.lambda_function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.cert_key_changes.arn
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
      var.dynamodb_table_idpMetadata.table_arn,
      var.dynamodb_table_idpMetadata.gsi_pointer_arn
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
  egress_prefix_list_ids = [
    var.metadata_lambda.vpc_endpoint_dynamodb_prefix_id,
    var.idp_metadata_lambda.vpc_s3_prefix_id,
  ]

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
data "aws_ssm_parameter" "is_gh_integration_lambda" {
  name = var.is_gh_integration_lambda.ssm_parameter_name
}

data "aws_iam_policy_document" "is_gh_integration_lambda" {
  statement {
    effect = "Allow"
    actions = [
      "ssm:Describe*",
      "ssm:Get*",
      "ssm:List*"
    ]
    resources = [data.aws_ssm_parameter.is_gh_integration_lambda.arn]
  }
}

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

  attach_policy_json = true
  policy_json        = data.aws_iam_policy_document.is_gh_integration_lambda.json

  cloudwatch_logs_retention_in_days = var.is_gh_integration_lambda.cloudwatch_logs_retention_in_days

  environment_variables = var.is_gh_integration_lambda.environment_variables

  allowed_triggers = [
    {
      sns = {
        principal  = "sns.amazonaws.com"
        source_arn = var.is_gh_integration_lambda.sns_topic_arn
      }
    }, {}
    ][
    var.is_gh_integration_lambda.sns_topic_arn != null ? 0 : 1
  ]

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
    actions   = ["cloudwatch:PutMetricData"]
    resources = ["*"]
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
    var.assertion_lambda.vpc_s3_prefix_id,
  ]
  egress_rules = ["https-443-tcp"]
}


resource "aws_vpc_security_group_egress_rule" "https_rule" {
  security_group_id            = module.security_group_lambda_assertion.security_group_id
  from_port                    = 443
  ip_protocol                  = "tcp"
  to_port                      = 443
  referenced_security_group_id = var.assertion_lambda.vpc_tls_security_group_id
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

  alarm_actions = each.value.sns_topic_alarm_arn != null ? [each.value.sns_topic_alarm_arn] : []
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

  alarm_actions = var.dlq_alarms.sns_topic_alarm_arn != null ? [var.dlq_alarms.sns_topic_alarm_arn] : []
}


## Lambda update IDP and Client status

data "aws_iam_policy_document" "update_status_lambda" {
  statement {
    effect    = "Allow"
    actions   = ["s3:PutObject", "s3:GetObject"]
    resources = ["${var.update_status_lambda.assets_bucket_arn}/*"]
  }
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:DeleteItem",
      "dynamodb:Query",
      "dynamodb:PutItem"
    ]
    resources = [
      var.dynamodb_table_idpStatus.table_arn,
      var.dynamodb_table_idpStatus.gsi_pointer_arn,
      var.dynamodb_table_clientStatus.table_arn,
      var.dynamodb_table_clientStatus.gsi_pointer_arn
    ]
  }
}


module "security_group_update_status_lambda" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "4.17.2"

  name        = "${var.update_status_lambda.name}-sg"
  description = "Security Group for Lambda Update IDP Status"

  vpc_id = var.update_status_lambda.vpc_id

  egress_cidr_blocks      = []
  egress_ipv6_cidr_blocks = []

  # Prefix list ids to use in all egress rules in this module
  egress_prefix_list_ids = [
    var.update_status_lambda.vpc_endpoint_dynamodb_prefix_id,
    var.update_status_lambda.vpc_s3_prefix_id,
  ]
  egress_rules = ["https-443-tcp"]
}


module "update_status_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.update_status_lambda.name
  description             = "Lambda function update status."
  runtime                 = "python3.12"
  handler                 = "lambda.lambda_handler"
  create_package          = false
  local_existing_package  = var.update_status_lambda.filename
  ignore_source_code_hash = true

  publish = true

  attach_policy_json = true
  policy_json        = data.aws_iam_policy_document.update_status_lambda.json


  cloudwatch_logs_retention_in_days = var.update_status_lambda.cloudwatch_logs_retention_in_days

  environment_variables = var.update_status_lambda.environment_variables

  attach_network_policy = true

  vpc_subnet_ids         = var.update_status_lambda.vpc_subnet_ids
  vpc_security_group_ids = [module.security_group_update_status_lambda.security_group_id]

  allowed_triggers = {
    IDPErrorRate = {
      principal  = "lambda.alarms.cloudwatch.amazonaws.com"
      source_arn = "arn:aws:cloudwatch:${var.aws_region}:${var.account_id}:alarm:IDPErrorRateAlarm*"
    },
    ClientErrorRate = {
      principal  = "lambda.alarms.cloudwatch.amazonaws.com"
      source_arn = "arn:aws:cloudwatch:${var.aws_region}:${var.account_id}:alarm:ClientErrorRateAlarm*"
    },
    IDPNoTrafficErrorRate = {
      principal  = "lambda.alarms.cloudwatch.amazonaws.com"
      source_arn = "arn:aws:cloudwatch:${var.aws_region}:${var.account_id}:alarm:IDPNoTrafficErrorRateAlarm*"
    },
    ClientNoTrafficErrorRate = {
      principal  = "lambda.alarms.cloudwatch.amazonaws.com"
      source_arn = "arn:aws:cloudwatch:${var.aws_region}:${var.account_id}:alarm:ClientNoTrafficErrorRateAlarm*"
    }
  }

  memory_size = 256
  timeout     = 30

}

## Lambda retrieve status
data "aws_iam_policy_document" "retrieve_status_lambda" {

  statement {
    effect = "Allow"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:Query",
      "dynamodb:Scan"
    ]
    resources = [
      var.dynamodb_table_idpStatus.table_arn,
      var.dynamodb_table_clientStatus.table_arn
    ]
  }
}


module "security_group_retrieve_status_lambda" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "4.17.2"

  name        = "${var.retrieve_status_lambda.name}-sg"
  description = "Security Group for Lambda Retrieve Status"

  vpc_id = var.retrieve_status_lambda.vpc_id

  egress_cidr_blocks      = []
  egress_ipv6_cidr_blocks = []

  # Prefix list ids to use in all egress rules in this module
  egress_prefix_list_ids = [
    var.retrieve_status_lambda.vpc_endpoint_dynamodb_prefix_id
  ]
  egress_rules = ["https-443-tcp"]
}


module "retrieve_status_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.retrieve_status_lambda.name
  description             = "Lambda function retrieve status."
  runtime                 = "python3.12"
  handler                 = "lambda.lambda_handler"
  create_package          = false
  local_existing_package  = var.retrieve_status_lambda.filename
  ignore_source_code_hash = true

  publish = true

  attach_policy_json = true
  policy_json        = data.aws_iam_policy_document.retrieve_status_lambda.json

  cloudwatch_logs_retention_in_days = var.retrieve_status_lambda.cloudwatch_logs_retention_in_days

  environment_variables = var.retrieve_status_lambda.environment_variables

  attach_network_policy = true

  vpc_subnet_ids         = var.retrieve_status_lambda.vpc_subnet_ids
  vpc_security_group_ids = [module.security_group_retrieve_status_lambda.security_group_id]

  memory_size = 256
  timeout     = 30

  allowed_triggers = {
    StatusAPIGateway = {
      service    = "apigateway"
      source_arn = "arn:aws:execute-api:${var.aws_region}:${var.account_id}:${var.rest_api_id}/*/GET/monitor/{type}/status"
    }
  }
}

# Lambda invalidate cache

module "invalidate_cache_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.invalidate_cache_lambda.name
  description             = "Lambda function invalidate cache."
  runtime                 = "python3.12"
  handler                 = "index.lambda_handler"
  create_package          = false
  local_existing_package  = var.invalidate_cache_lambda.filename
  ignore_source_code_hash = true

  publish = true

  attach_policy_json = true
  policy_json        = data.aws_iam_policy_document.invalidate_cache_lambda.json


  cloudwatch_logs_retention_in_days = var.invalidate_cache_lambda.cloudwatch_logs_retention_in_days

  environment_variables = var.invalidate_cache_lambda.environment_variables

  # allowed_triggers = {
  #   dynamodb = {
  #     principal  = "dynamodb.amazonaws.com"
  #     source_arn = var.dynamodb_clients_table_stream_arn
  #   }
  # }

  allowed_triggers = {
    events = {
      principal  = "events.amazonaws.com"
      source_arn = aws_pipes_pipe.invalidate_cache.arn
    }
  }

  memory_size = 256
  timeout     = 30

}


data "aws_iam_policy_document" "invalidate_cache_lambda" {

  statement {
    effect = "Allow"
    actions = [
      "apigateway:UpdateRestApi",
      "apigateway:FlushStageCache",
      "apigateway:DELETE"
    ]
    resources = [
      "${var.invalidate_cache_lambda.rest_api_arn}/*",
      "${var.invalidate_cache_lambda.rest_api_execution_arn}/*"
    ]
  }
  # statement {
  #   effect = "Allow"
  #   actions = [
  #     "dynamodb:DescribeStream",
  #     "dynamodb:GetRecords",
  #     "dynamodb:GetShardIterator",
  #     "dynamodb:ListStreams",
  #   ]
  #   resources = [var.dynamodb_clients_table_stream_arn]
  # }
}

# module "security_group_invalidate_cache_lambda" {
#   source  = "terraform-aws-modules/security-group/aws"
#   version = "4.17.2"

#   name        = "${var.invalidate_cache_lambda.name}-sg"
#   description = "Security Group for Lambda Invalidate Cache"

#   vpc_id = var.invalidate_cache_lambda.vpc_id

#   egress_cidr_blocks      = []
#   egress_ipv6_cidr_blocks = []

#   # Prefix list ids to use in all egress rules in this module
#   egress_prefix_list_ids = [
#     var.invalidate_cache_lambda.vpc_endpoint_apigw_prefix_id,
#     var.invalidate_cache_lambda.vpc_endpoint_dynamodb_prefix_id
#   ]

#   # egress_rules = ["https-443-tcp"]

# }

# resource "aws_lambda_event_source_mapping" "invalidate_cache_trigger" {
#   depends_on = [
#     module.invalidate_cache_lambda.lambda_function_name,
#     var.table_client_registrations_arn
#   ]
#   event_source_arn  = var.dynamodb_clients_table_stream_arn
#   function_name     = module.invalidate_cache_lambda.lambda_function_arn
#   starting_position = "LATEST"
#   enabled           = true
# }

# Lambda client manager

resource "null_resource" "install_client_manager_dependencies" {
  provisioner "local-exec" {
    command = <<EOT
      mkdir -p ${path.module}/../../dist/python
      pip install \
        --platform manylinux2014_x86_64 \
        --target=${path.module}/../../dist/python \
        --implementation cp \
        --only-binary=:all: --upgrade \
        -r ../../../oneid/oneid-lambda-client-manager/requirements.txt
    EOT
  }

  triggers = {
    requirements_hash = filemd5("${path.module}/../../../oneid/oneid-lambda-client-manager/requirements.txt")
  }
}

data "archive_file" "pyjwt_layer" {
  type        = "zip"
  source_dir  = "${path.module}/../../dist/"
  output_path = "${path.module}/../../dist/python.zip"
  depends_on  = [null_resource.install_client_manager_dependencies]
}

resource "aws_lambda_layer_version" "pyjwt_layer" {
  layer_name          = "pyjwt-layer"
  description         = "Lambda layer with PyJWT"
  compatible_runtimes = ["python3.12"]
  filename            = data.archive_file.pyjwt_layer.output_path
  source_code_hash    = data.archive_file.pyjwt_layer.output_base64sha256
}

module "client_manager_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.client_manager_lambda.name
  description             = "Lambda function client manager."
  runtime                 = "python3.12"
  handler                 = "index.handler"
  create_package          = false
  local_existing_package  = var.client_manager_lambda.filename
  ignore_source_code_hash = true

  attach_policy_json = true
  policy_json        = data.aws_iam_policy_document.client_manager_lambda.json

  publish = true

  cloudwatch_logs_retention_in_days = var.client_manager_lambda.cloudwatch_logs_retention_in_days

  environment_variables = var.client_manager_lambda.environment_variables

  # lambda powertools layer
  layers = [
    "arn:aws:lambda:${var.aws_region}:017000801446:layer:AWSLambdaPowertoolsPythonV3-python312-x86_64:11",
    aws_lambda_layer_version.pyjwt_layer.arn
  ]

  memory_size = 256
  timeout     = 30

}

data "aws_iam_policy_document" "client_manager_lambda" {
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:UpdateItem",
      "dynamodb:GetItem",
      "dynamodb:Scan"
    ]
    resources = [
      var.client_manager_lambda.table_client_registrations_arn
    ]
  }

  dynamic "statement" {
    for_each = var.client_manager_lambda.cognito_user_pool_arn != "" ? [1] : []
    content {
      effect = "Allow"
      actions = [
        "cognito-idp:AdminUpdateUserAttributes",
        "cognito-idp:AdminGetUser"
      ]
      resources = [
        var.client_manager_lambda.cognito_user_pool_arn
      ]
    }
  }

  dynamic "statement" {
    for_each = var.client_manager_lambda_optional_iam_policy != false ? [1] : []
    content {
      effect = "Allow"
      actions = [
        "dynamodb:UpdateItem",
        "dynamodb:GetItem",
        "dynamodb:Query",
        "dynamodb:DeleteItem",
        "dynamodb:PutItem"
      ]
      resources = [
        var.client_manager_lambda.table_idp_internal_users_arn,
        var.client_manager_lambda.table_idp_internal_users_gsi_arn
      ]
    }
  }
}

# Lambda PDV reconciler
module "pdv_reconciler_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.pdv_reconciler_lambda.name
  description             = "Lambda function PDV reconciler."
  runtime                 = "python3.12"
  handler                 = "index.handler"
  create_package          = false
  local_existing_package  = var.pdv_reconciler_lambda.filename
  ignore_source_code_hash = true


  attach_policy_json = true
  policy_json        = data.aws_iam_policy_document.pdv_reconciler_lambda.json

  publish = true

  cloudwatch_logs_retention_in_days = var.pdv_reconciler_lambda.cloudwatch_logs_retention_in_days

  environment_variables = var.pdv_reconciler_lambda.environment_variables

  attach_network_policy = true

  vpc_subnet_ids         = var.pdv_reconciler_lambda.vpc_subnet_ids
  vpc_security_group_ids = [module.security_group_lambda_pdv_reconciler.security_group_id]

  allowed_triggers = {
    AllowSQSTrigger = {
      principal  = "sqs.amazonaws.com"
      source_arn = var.pdv_reconciler_lambda.pdv_errors_queue_arn
    }
  }

  event_source_mapping = {
    sqs_trigger = {
      event_source_arn = var.pdv_reconciler_lambda.pdv_errors_queue_arn
      enabled          = true
    }
  }
  memory_size = 256
  timeout     = 30

}

module "security_group_lambda_pdv_reconciler" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "4.17.2"

  name        = "${var.pdv_reconciler_lambda.name}-sg"
  description = "Security Group for Lambda PDV Reconciler"

  vpc_id = var.pdv_reconciler_lambda.vpc_id

  egress_cidr_blocks      = []
  egress_ipv6_cidr_blocks = []

  egress_rules = ["https-443-tcp"]
}

resource "aws_vpc_security_group_egress_rule" "pdv_reconciler_https_rule" {
  security_group_id = module.security_group_lambda_pdv_reconciler.security_group_id
  from_port         = 443
  ip_protocol       = "tcp"
  to_port           = 443
  cidr_ipv4         = "0.0.0.0/0"
}

data "aws_iam_policy_document" "pdv_reconciler_lambda" {
  statement {
    effect = "Allow"
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes"
    ]
    resources = [
      var.pdv_reconciler_lambda.pdv_errors_queue_arn
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "ssm:Describe*",
      "ssm:Get*",
      "ssm:List*"
    ]
    resources = [
      "arn:aws:ssm:${var.aws_region}:${var.account_id}:parameter/pdv/*"
    ]
  }

}

## Cert Expiration Lambda ##
data "aws_iam_policy_document" "cert_exp_checker_lambda" {
  statement {
    effect = "Allow"
    actions = [
      "ssm:Describe*",
      "ssm:Get*",
      "ssm:List*"
    ]
    resources = [data.aws_ssm_parameter.certificate.arn]
  }

  statement {
    effect    = "Allow"
    actions   = ["cloudwatch:PutMetricData"]
    resources = ["*"]
  }

  statement {
    effect    = "Allow"
    actions   = ["sns:Publish"]
    resources = [var.cert_exp_checker_lambda.sns_topic_arn]
  }
}


module "security_group_lambda_cert_exp_checker" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "4.17.2"

  name        = "${var.cert_exp_checker_lambda.name}-sg"
  description = "Security Group for Lambda Cert Expiration"

  vpc_id = var.cert_exp_checker_lambda.vpc_id

  egress_cidr_blocks      = []
  egress_ipv6_cidr_blocks = []

  # Prefix list ids to use in all egress rules in this module
  egress_prefix_list_ids = [
  ]
  egress_rules = ["https-443-tcp"]
}

resource "aws_vpc_security_group_egress_rule" "cert_checker_sec_group_egress_rule" {
  security_group_id            = module.security_group_lambda_cert_exp_checker.security_group_id
  from_port                    = 443
  ip_protocol                  = "tcp"
  to_port                      = 443
  referenced_security_group_id = var.cert_exp_checker_lambda.vpc_tls_security_group_endpoint_id
}

resource "null_resource" "install_dependencies" {
  provisioner "local-exec" {
    command = <<EOT
      mkdir -p ${path.module}/../../dist/python
      pip install \
        --platform manylinux2014_x86_64 \
        --target=${path.module}/../../dist/python \
        --implementation cp \
        --only-binary=:all: --upgrade \
        -r ../../../oneid/oneid-lambda-cert-exp-checker/requirements.txt
    EOT
  }

  triggers = {
    requirements_hash = filemd5("${path.module}/../../../oneid/oneid-lambda-cert-exp-checker/requirements.txt")
  }
}

data "archive_file" "cryptography_layer" {
  type        = "zip"
  source_dir  = "${path.module}/../../dist/"
  output_path = "${path.module}/../../dist/python.zip"
  depends_on  = [null_resource.install_dependencies]
}

resource "aws_lambda_layer_version" "cryptography" {
  layer_name          = "cryptography-layer"
  description         = "Lambda layer with cryptography"
  compatible_runtimes = ["python3.10"]
  filename            = data.archive_file.cryptography_layer.output_path
  source_code_hash    = data.archive_file.cryptography_layer.output_base64sha256
}

module "cert_exp_checker_lambda" {
  source                 = "terraform-aws-modules/lambda/aws"
  version                = "7.4.0"
  function_name          = var.cert_exp_checker_lambda.name
  description            = "Lambda function cert expiration checker."
  runtime                = "python3.10"
  handler                = "index.lambda_handler"
  create_package         = false
  local_existing_package = var.cert_exp_checker_lambda.filename
  layers                 = [aws_lambda_layer_version.cryptography.arn]

  ignore_source_code_hash = true

  publish = true

  attach_policy_json = true
  policy_json        = data.aws_iam_policy_document.cert_exp_checker_lambda.json

  environment_variables = var.cert_exp_checker_lambda.environment_variables

  attach_network_policy = true

  vpc_subnet_ids         = var.cert_exp_checker_lambda.vpc_subnet_ids
  vpc_security_group_ids = [module.security_group_lambda_cert_exp_checker.security_group_id]

  memory_size = 512
  timeout     = 30

  cloudwatch_logs_retention_in_days = var.cert_exp_checker_lambda.cloudwatch_logs_retention_in_days

}

resource "aws_cloudwatch_event_rule" "cert_expiration" {
  name        = "Lambda-cert-exp-checker-schedule"
  description = "Trigger lambda cert-exp-checker every week"

  schedule_expression = var.cert_exp_checker_lambda.schedule_expression
}

resource "aws_cloudwatch_event_target" "cert_exp_checker_lambda" {
  rule = aws_cloudwatch_event_rule.cert_expiration.name
  arn  = module.cert_exp_checker_lambda.lambda_function_arn
}

resource "aws_lambda_permission" "allow_eventbridge" {
  action        = "lambda:InvokeFunction"
  function_name = module.cert_exp_checker_lambda.lambda_function_arn
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.cert_expiration.arn
}


