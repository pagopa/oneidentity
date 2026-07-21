## Event bridge pipe collecting dynamodb stream

resource "aws_sqs_queue" "pipe_dlq" {
  name = "${var.eventbridge_pipe_sessions.pipe_name}-dlq"
}

resource "aws_cloudwatch_metric_alarm" "dlq_sessions" {
  alarm_name = format("%s-%s-Dlq-%s", var.eventbridge_pipe_sessions.pipe_name, var.dlq_alarms.metric_name,
  var.dlq_alarms.threshold)
  comparison_operator = var.dlq_alarms.comparison_operator
  evaluation_periods  = var.dlq_alarms.evaluation_periods
  metric_name         = var.dlq_alarms.metric_name
  namespace           = var.dlq_alarms.namespace
  period              = var.dlq_alarms.period
  statistic           = var.dlq_alarms.statistic
  threshold           = var.dlq_alarms.threshold


  dimensions = {
    QueueName = aws_sqs_queue.pipe_dlq.name
  }

  alarm_actions = var.dlq_alarms.sns_topic_alarm_arn != null ? [var.dlq_alarms.sns_topic_alarm_arn] : []
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
  "ipAddress": <$.dynamodb.NewImage.ipAddress.S>
}
EOF
  }
}

resource "aws_iam_role" "pipe_invalidate_cache" {
  name = "${var.eventbridge_pipe_invalidate_cache.pipe_name}-role"

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

resource "aws_iam_role_policy" "pipe_cache_source" {
  name = "AllowConsumeStreamAndInvokeLambda"

  role = aws_iam_role.pipe_invalidate_cache.id
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
          var.dynamodb_table_stream_registrations_arn
        ]
      },
      {
        Sid    = "InvokeLambdaInvalidateCache"
        Effect = "Allow"
        Action = [
          "lambda:InvokeFunction"
        ]
        Resource = [
          module.invalidate_cache_lambda.lambda_function_arn
        ]
      },
    ]
  })
}

resource "aws_pipes_pipe" "invalidate_cache" {
  name     = var.eventbridge_pipe_invalidate_cache.pipe_name
  role_arn = aws_iam_role.pipe_invalidate_cache.arn
  source   = var.dynamodb_table_stream_registrations_arn

  target = module.invalidate_cache_lambda.lambda_function_arn

  source_parameters {
    dynamodb_stream_parameters {
      starting_position = "LATEST"

      maximum_retry_attempts        = var.eventbridge_pipe_invalidate_cache.maximum_retry_attempts
      maximum_record_age_in_seconds = var.eventbridge_pipe_invalidate_cache.maximum_record_age_in_seconds

    }
  }

  target_parameters {
    input_template = <<EOF
{
  "clientId": "<$.dynamodb.NewImage.clientId.S>"
}
EOF
  }
}


resource "aws_iam_role" "pipe_update_idp_metadata" {
  count = var.eventbridge_pipe_update_idp_metadata != null && var.idp_metadata_stream_trigger_enabled ? 1 : 0

  name = "${var.eventbridge_pipe_update_idp_metadata.pipe_name}-role"

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

resource "aws_iam_role_policy" "pipe_update_idp_metadata" {
  count = var.eventbridge_pipe_update_idp_metadata != null && var.idp_metadata_stream_trigger_enabled ? 1 : 0

  name = "AllowReadIdpMetadataStreamAndPublishInvalidStatus"

  role = aws_iam_role.pipe_update_idp_metadata[0].id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "ReadIdpMetadataStream"
        Effect = "Allow"
        Action = [
          "dynamodb:GetRecords",
          "dynamodb:GetShardIterator",
          "dynamodb:DescribeStream",
          "dynamodb:ListStreams"
        ]
        Resource = var.dynamodb_table_idpMetadata.stream_arn
      },
      {
        Sid      = "PublishInvalidIdpStatus"
        Effect   = "Allow"
        Action   = "sns:Publish"
        Resource = var.sns_topic_arn
      }
    ]
  })
}

resource "aws_pipes_pipe" "update_idp_metadata" {
  count = var.eventbridge_pipe_update_idp_metadata != null && var.idp_metadata_stream_trigger_enabled ? 1 : 0

  name     = var.eventbridge_pipe_update_idp_metadata.pipe_name
  role_arn = aws_iam_role.pipe_update_idp_metadata[0].arn
  source   = var.dynamodb_table_idpMetadata.stream_arn

  target = var.sns_topic_arn

  source_parameters {
    dynamodb_stream_parameters {
      starting_position = "LATEST"

      maximum_batching_window_in_seconds = 0
      maximum_retry_attempts             = var.eventbridge_pipe_update_idp_metadata.maximum_retry_attempts
      maximum_record_age_in_seconds      = var.eventbridge_pipe_update_idp_metadata.maximum_record_age_in_seconds
    }

    filter_criteria {
      filter {
        pattern = jsonencode({
          "eventName" = ["MODIFY"]
          "dynamodb" = {
            "NewImage" = {
              "status" = {
                "S" = [{
                  "anything-but" = ["OK", "KO", "WARNING"]
                }]
              }
            }
          }
        })
      }
    }
  }

  target_parameters {
    input_template = <<EOF
{
  "table_name": "${element(split("/", var.dynamodb_table_idpMetadata.table_arn), 1)}",
  "idp": "<$.dynamodb.NewImage.entityID.S>",
  "status": {
    "previous": "<$.dynamodb.OldImage.status.S>",
    "current": "<$.dynamodb.NewImage.status.S>"
  }
}
EOF
  }
}

resource "aws_sqs_queue" "client_publisher_dlq" {
  count = var.client_publisher_lambda != null && var.eventbridge_pipe_client_publisher != null ? 1 : 0

  name                      = "${var.eventbridge_pipe_client_publisher.pipe_name}-dlq"
  sqs_managed_sse_enabled   = true
  message_retention_seconds = 1209600
}


resource "aws_iam_role" "client_publisher_pipe" {
  count = var.client_publisher_lambda != null && var.eventbridge_pipe_client_publisher != null ? 1 : 0

  name = "${var.eventbridge_pipe_client_publisher.pipe_name}-role"

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

resource "aws_iam_role_policy" "client_publisher_pipe" {
  count = var.client_publisher_lambda != null && var.eventbridge_pipe_client_publisher != null ? 1 : 0

  name = "AllowConsumeStreamInvokeLambdaAndSendDlq"
  role = aws_iam_role.client_publisher_pipe[0].id

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
        ]
        Resource = [var.dynamodb_table_stream_registrations_arn]
      },
      {
        Sid      = "InvokeLambdaTarget"
        Effect   = "Allow"
        Action   = ["lambda:InvokeFunction"]
        Resource = [module.client_publisher_lambda[0].lambda_function_arn]
      },
      {
        Sid      = "SendMessageToDlq"
        Effect   = "Allow"
        Action   = ["sqs:SendMessage"]
        Resource = [aws_sqs_queue.client_publisher_dlq[0].arn]
      },
    ]
  })
}

resource "aws_pipes_pipe" "client_publisher" {
  count = var.client_publisher_lambda != null && var.eventbridge_pipe_client_publisher != null ? 1 : 0

  depends_on = [aws_iam_role_policy.client_publisher_pipe]

  name     = var.eventbridge_pipe_client_publisher.pipe_name
  role_arn = aws_iam_role.client_publisher_pipe[0].arn
  source   = var.dynamodb_table_stream_registrations_arn
  target   = module.client_publisher_lambda[0].lambda_function_arn

  source_parameters {
    dynamodb_stream_parameters {
      starting_position = "LATEST"

      maximum_retry_attempts        = var.eventbridge_pipe_client_publisher.maximum_retry_attempts
      maximum_record_age_in_seconds = var.eventbridge_pipe_client_publisher.maximum_record_age_in_seconds

      dead_letter_config {
        arn = aws_sqs_queue.client_publisher_dlq[0].arn
      }
    }

    filter_criteria {
      filter {
        pattern = jsonencode({ "eventName" : ["INSERT", "MODIFY", "REMOVE"] })
      }
    }
  }

  target_parameters {
    input_template = <<EOF
{
  "eventName": <$.eventName>,
  "dynamodb": {
    "NewImage": {
      "clientId":            <$.dynamodb.NewImage.clientId>,
      "friendlyName":        <$.dynamodb.NewImage.friendlyName>,
      "logoUri":             <$.dynamodb.NewImage.logoUri>,
      "policyUri":           <$.dynamodb.NewImage.policyUri>,
      "tosUri":              <$.dynamodb.NewImage.tosUri>,
      "a11yUri":             <$.dynamodb.NewImage.a11yUri>,
      "backButtonEnabled":   <$.dynamodb.NewImage.backButtonEnabled>,
      "localizedContentMap": <$.dynamodb.NewImage.localizedContentMap>,
      "samlBinding":         <$.dynamodb.NewImage.samlBinding>,
      "callbackURI":         <$.dynamodb.NewImage.callbackURI>,
      "active":              <$.dynamodb.NewImage.active>
    },
    "OldImage": {
      "clientId":            <$.dynamodb.OldImage.clientId>,
      "friendlyName":        <$.dynamodb.OldImage.friendlyName>,
      "logoUri":             <$.dynamodb.OldImage.logoUri>,
      "policyUri":           <$.dynamodb.OldImage.policyUri>,
      "tosUri":              <$.dynamodb.OldImage.tosUri>,
      "a11yUri":             <$.dynamodb.OldImage.a11yUri>,
      "backButtonEnabled":   <$.dynamodb.OldImage.backButtonEnabled>,
      "localizedContentMap": <$.dynamodb.OldImage.localizedContentMap>,
      "samlBinding":         <$.dynamodb.OldImage.samlBinding>,
      "callbackURI":         <$.dynamodb.OldImage.callbackURI>,
      "active":              <$.dynamodb.OldImage.active>
    }
  }
}
EOF
  }
}

resource "aws_cloudwatch_metric_alarm" "client_publisher_dlq" {
  count = var.client_publisher_lambda != null && var.eventbridge_pipe_client_publisher != null ? 1 : 0

  alarm_name = format(
    "%s-%s-Dlq-%s",
    var.eventbridge_pipe_client_publisher.pipe_name,
    var.dlq_alarms.metric_name,
    var.dlq_alarms.threshold
  )
  comparison_operator = var.dlq_alarms.comparison_operator
  evaluation_periods  = var.dlq_alarms.evaluation_periods
  metric_name         = var.dlq_alarms.metric_name
  namespace           = var.dlq_alarms.namespace
  period              = var.dlq_alarms.period
  statistic           = var.dlq_alarms.statistic
  threshold           = var.dlq_alarms.threshold

  dimensions = {
    QueueName = aws_sqs_queue.client_publisher_dlq[0].name
  }

  alarm_actions = var.dlq_alarms.sns_topic_alarm_arn != null ? [var.dlq_alarms.sns_topic_alarm_arn] : []
}
