locals {
  kms_sessions_table_alias = "/dynamodb/sessions"
  gsi_code                 = "gsi_code_idx"
}


module "kms_sessions_table" {
  source  = "terraform-aws-modules/kms/aws"
  version = "2.2.1"

  description = "KMS key for Dynamodb table encryption."
  key_usage   = "ENCRYPT_DECRYPT"

  # Aliases
  aliases = [local.kms_sessions_table_alias]
}

module "dynamodb_sessions_table" {
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = "Sessions"

  hash_key  = "samlRequestID"
  range_key = "recordType"

  global_secondary_indexes = [
    {
      name            = local.gsi_code
      hash_key        = "code"
      projection_type = "ALL"
    }
  ]

  attributes = [
    {
      name = "samlRequestID"
      type = "S"
    },
    {
      name = "recordType"
      type = "S"
    },
    {
      name = "code"
      type = "S"
    }
  ]

  ttl_attribute_name = "ttl"
  ttl_enabled        = var.sessions_table.ttl_enabled

  billing_mode = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.sessions_table.point_in_time_recovery_enabled

  server_side_encryption_enabled     = true
  server_side_encryption_kms_key_arn = module.kms_sessions_table.aliases[local.kms_sessions_table_alias].target_key_arn

  stream_enabled   = var.sessions_table.stream_enabled
  stream_view_type = var.sessions_table.stream_view_type

  tags = {
    Name = "Session"
  }

}

module "dynamodb_table_client_registrations" {
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = "ClientRegistrations"

  hash_key = "clientId"

  attributes = [
    {
      name = "clientId"
      type = "S"
    }
  ]

  billing_mode = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.sessions_table.point_in_time_recovery_enabled

  tags = {
    Name = "ClientRegistrations"
  }

}

## Eventbridge pipe ## 


# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "pipe_logs" {
  name              = "/aws/pipes/dynamodb-to-cloudwatch-pipe"
  retention_in_days = 14 # Adjust as needed
}

resource "aws_iam_role" "pipe_sessions" {
  count = var.eventbridge_pipe_sessions != null ? 1 : 0
  name  = "${var.eventbridge_pipe_sessions.pipe_name}-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = {
      Effect = "Allow"
      Action = "sts:AssumeRole"
      Principal = {
        Service = "pipes.amazonaws.com"
      }
      /*
      Condition = {
        StringEquals = {
          "aws:SourceAccount" = var.account_id
        }
      } */
    }
  })
}

resource "aws_iam_role_policy" "pipe_source" {
  #count = var.eventbridge_pipe_sessions != null ? 1 : 0
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
          module.kms_sessions_table.aliases[local.kms_sessions_table_alias].target_key_arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "${aws_cloudwatch_log_group.pipe_logs.arn}:*"
      },
    ]
  })
}


resource "aws_pipes_pipe" "dynamodb_to_cloudwatch" {
  name     = "dynamodb-to-cloudwatch-pipe"
  role_arn = aws_iam_role.pipe_sessions[0].arn
  source   = module.dynamodb_sessions_table.dynamodb_table_stream_arn

  target = aws_cloudwatch_log_group.pipe_logs.arn

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
