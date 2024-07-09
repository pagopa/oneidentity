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

data "aws_iam_policy_document" "metadata_lambda" {
  //name = format("%s-task-policy", var.metadata_lambda.name)
  //policy = jsonencode({
  //Version = "2012-10-17"
  statement {
    effect    = "Allow"
    actions   = ["dynamodb:Scan"]
    resources = ["${var.table_client_registrations_arn}"]
  }

}

## Lambda metadata

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

  attach_policy_json = true
  policy_json        = data.aws_iam_policy_document.metadata_lambda.json

  environment_variables = var.metadata_lambda.environment_variables

  memory_size = 512
  timeout     = 30
  snap_start  = true

}
resource "aws_iam_role" "pipe_sessions" {
  count = var.sessions_table.stream_enabled != null ? 1 : 0
  name  = "${var.eventbridge_pipe_sessions.pipe_name}-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = {
      Effect = "Allow"
      Action = "sts:AssumeRole"
      Principal = {
        Service = "pipes.amazonaws.com"
      }
      Condition = {
        StringEquals = {
          "aws:SourceAccount" = var.account_id
        }
      }
    }
  })
}

resource "aws_iam_role_policy" "pipe_source" {
  count = var.sessions_table.stream_enabled != null ? 1 : 0
  name = "AllowPipeConsumeStream"

  role = aws_iam_role.pipe_sessions[0].id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetRecords",
          "dynamodb:GetShardIterator",
          "dynamodb:DescribeStream",
          "dynamodb:ListStreams"
        ],
        Resource = [
          module.dynamodb_sessions_table.dynamodb_table_stream_arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt",
          "kms:Encrypt",
        ],
        Resource = [
          var.assertion_lambda.kms_sessions_table_alias
        ]
      },
      {
        #TODO this statement is not needed as soon as there will be a lambda function reading the pipe.
        Effect = "Allow"
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "${aws_cloudwatch_log_group.pipe_logs[0].arn}:*"
      },
    ]
  })
}


#TODO rename this resource and replace the targe.
resource "aws_pipes_pipe" "dynamodb_to_lambda" {
  count = var.sessions_table.stream_enabled ? 1: 0
  name     = "dynamodb-to-lambda-pipe"
  role_arn = aws_iam_role.pipe_sessions[0].arn
  source   = module.dynamodb_sessions_table.dynamodb_table_stream_arn

  target = module.assertion_lambda[0].lambda_function_arn
  //target = aws_cloudwatch_log_group.pipe_logs[0].arn

  source_parameters {
    dynamodb_stream_parameters {
      starting_position = "LATEST"
    }
    filter_criteria {
      filter {
        pattern = jsonencode(
          {
            "$or" : [{
              "dynamodb" : {
                "NewImage" : {
                  "recordType" : {
                    "S" : ["SAML"]
                  }
                }
              },
              "eventName" : ["MODIFY"]
              }, {
              "dynamodb" : {
                "NewImage" : {
                  "recordType" : {
                    "S" : ["ACCESS_TOKEN"]
                  }
                }
              },
              "eventName" : ["INSERT"]
            }]
          }
        )
      }
    }
  }

  /*
  target_parameters {
    input_template = <<EOF
{
  "timestamp": <$.dynamodb.ApproximateCreationDateTime>,
  "eventName": <$.eventName>,
  "recordType": <$.dynamodb.NewImage.recordType.S>,
  "id": <$.dynamodb.NewImage.id.S>
}
EOF
  }
*/
}



module "assertion_lambda" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "7.4.0"

  function_name           = var.assertion_lambda.name
  description             = "Lambda function assertion."
  runtime                 = "python3.8"
  handler                 = "assertion.lambda_handler"
  create_package          = false
  local_existing_package  = var.assertion_lambda.filename
  //ignore_source_code_hash = true

  publish = true

  //attach_policy_json = true
  //policy_json        = data.aws_iam_policy_document.metadata_lambda.json

  //environment_variables = var.metadata_lambda.environment_variables

  allowed_triggers = {
    OneRule = {
      principal  = "events.amazonaws.com"
      source_arn = aws_pipes_pipe.dynamodb_to_lambda.arn
      //source_arn = "arn:aws:events:eu-west-1:${data.aws_caller_identity.current.account_id}:rule/RunDaily"
    }
  }

  memory_size = 512
  timeout     = 30
  
}