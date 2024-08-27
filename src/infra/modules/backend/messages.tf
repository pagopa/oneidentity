## Event bridge pipe collecting dynamodb stream

resource "aws_sqs_queue" "pipe_dlq" {
  name = "${var.eventbridge_pipe_sessions.pipe_name}-dlq"
}

resource "aws_iam_role" "pipe_sessions" {
  name = "${var.eventbridge_pipe_sessions.pipe_name}-role"

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
  name = "AllowConsumeStreamAndInvokeLambda"

  role = aws_iam_role.pipe_sessions.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "ReadFromStream"
        Effect = "Allow"
        Action = [
          "dynamodb:GetRecords",
          "dynamodb:GetShardIterator",
          "dynamodb:DescribeStream",
          "dynamodb:ListStreams"
        ],
        Resource = [
          var.dynamodb_table_stream_arn
        ]
      },
      {
        Sid    = "DecryptWithCustomerKey"
        Effect = "Allow"
        Action = [
          "kms:Decrypt",
          "kms:Encrypt",
        ]
        Resource = [
          var.eventbridge_pipe_sessions.kms_sessions_table_alias
        ]
      },
      {
        Sid    = "InvokeLambdaAssertion"
        Effect = "Allow"
        Action = [
          "lambda:InvokeFunction"
        ]
        Resource = [
          module.assertion_lambda.lambda_function_arn
        ]
      },
      {
        Action = [
          "sqs:SendMessage",
        ]
        Effect   = "Allow"
        Resource = aws_sqs_queue.pipe_dlq.arn
      },
    ]
  })
}

resource "aws_pipes_pipe" "sessions" {
  name     = var.eventbridge_pipe_sessions.pipe_name
  role_arn = aws_iam_role.pipe_sessions.arn
  source   = var.dynamodb_table_stream_arn

  target = module.assertion_lambda.lambda_function_arn

  source_parameters {
    dynamodb_stream_parameters {
      starting_position = "LATEST"

      dead_letter_config {
        arn = aws_sqs_queue.pipe_dlq.arn
      }
      maximum_retry_attempts        = var.eventbridge_pipe_sessions.maximum_retry_attempts
      maximum_record_age_in_seconds = var.eventbridge_pipe_sessions.maximum_record_age_in_seconds

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

  target_parameters {
    input_template = <<EOF
{
  "samlRequestID": <$.dynamodb.NewImage.samlRequestID.S>,
  "recordType": <$.dynamodb.NewImage.recordType.S>,
  "creationTime": <$.dynamodb.NewImage.creationTime.N>,
  "clientId": "<$.dynamodb.NewImage.clientId.S>",
  "idp": "<$.dynamodb.NewImage.idp.S>",
  "nonce": "<$.dynamodb.NewImage.nonce.S>",
  "redirectUri": "<$.dynamodb.NewImage.redirectUri.S>",
  "responseType": "<$.dynamodb.NewImage.responseType.S>",
  "SAMLRequest": <$.dynamodb.NewImage.SAMLRequest.S>,
  "SAMLResponse": <$.dynamodb.NewImage.SAMLResponse.S>,
  "scope": <$.dynamodb.NewImage.scope.S>,
  "state": <$.dynamodb.NewImage.state.S>,
  "ttl": <$.dynamodb.NewImage.ttl.N>,
  "code": <$.dynamodb.NewImage.code.S>,
  "idToken": <$.dynamodb.NewImage.idToken.S>,
  "eventName": <$.eventName>,
  "ipAddress": <$.ipAddress>
}
EOF
  }
}
