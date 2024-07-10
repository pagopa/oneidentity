## Event bridge pipe collecting dynamodb stream
resource "aws_iam_role" "pipe_sessions" {
  count = local.dynamodb_stream_enabled != null ? 1 : 0
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
  count = local.dynamodb_stream_enabled != null ? 1 : 0
  name  = "AllowPipeConsumeStream"

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
          var.dynamodb_table_stream_arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt",
          "kms:Encrypt",
        ],
        Resource = [
          var.eventbridge_pipe_sessions.kms_sessions_table_alias
        ]
      }
    ]
  })
}

resource "aws_pipes_pipe" "sessions" {
  count    = local.dynamodb_stream_enabled ? 1 : 0
  name     = var.eventbridge_pipe_sessions.pipe_name
  role_arn = aws_iam_role.pipe_sessions[0].arn
  source   = var.dynamodb_table_stream_arn

  target = module.assertion_lambda[0].lambda_function_arn

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

