## Cache updater EventBridge Pipe

resource "aws_sqs_queue" "cache_updater_dlq" {
  count = var.eventbridge_pipe_cache_updater != null ? 1 : 0

  name                      = "${var.eventbridge_pipe_cache_updater.pipe_name}-dlq"
  sqs_managed_sse_enabled   = true
  message_retention_seconds = 1209600
}

resource "aws_cloudwatch_metric_alarm" "cache_updater_dlq" {
  count = var.eventbridge_pipe_cache_updater != null ? 1 : 0

  alarm_name = format(
    "%s-%s-Dlq-%s",
    var.eventbridge_pipe_cache_updater.pipe_name,
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
    QueueName = aws_sqs_queue.cache_updater_dlq[0].name
  }

  alarm_actions = var.dlq_alarms.sns_topic_alarm_arn != null ? [var.dlq_alarms.sns_topic_alarm_arn] : []
}

resource "aws_iam_role" "pipe_cache_updater" {
  count = var.eventbridge_pipe_cache_updater != null ? 1 : 0

  name = "${var.eventbridge_pipe_cache_updater.pipe_name}-role"

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

resource "aws_iam_role_policy" "pipe_cache_updater_source" {
  count = var.eventbridge_pipe_cache_updater != null ? 1 : 0

  name = "AllowConsumeStreamInvokeLambdaAndSendDlq"
  role = aws_iam_role.pipe_cache_updater[0].id

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
        Sid      = "InvokeLambdaCacheUpdater"
        Effect   = "Allow"
        Action   = ["lambda:InvokeFunction"]
        Resource = [module.cache_updater_lambda[0].lambda_function_arn]
      },
      {
        Sid      = "SendMessageToCacheUpdaterDlq"
        Effect   = "Allow"
        Action   = ["sqs:SendMessage"]
        Resource = [aws_sqs_queue.cache_updater_dlq[0].arn]
      },
    ]
  })
}

resource "aws_pipes_pipe" "cache_updater" {
  count = var.eventbridge_pipe_cache_updater != null ? 1 : 0

  name     = var.eventbridge_pipe_cache_updater.pipe_name
  role_arn = aws_iam_role.pipe_cache_updater[0].arn
  source   = var.dynamodb_table_stream_registrations_arn
  target   = module.cache_updater_lambda[0].lambda_function_arn

  source_parameters {
    dynamodb_stream_parameters {
      starting_position = "LATEST"

      maximum_retry_attempts        = var.eventbridge_pipe_cache_updater.maximum_retry_attempts
      maximum_record_age_in_seconds = var.eventbridge_pipe_cache_updater.maximum_record_age_in_seconds

      dead_letter_config {
        arn = aws_sqs_queue.cache_updater_dlq[0].arn
      }
    }

    filter_criteria {
      filter {
        pattern = jsonencode({ "eventName" : ["INSERT"] })
      }
      filter {
        pattern = jsonencode([
          {
            "eventName" : ["MODIFY"],
            "dynamodb" : { "NewImage" : { "pairwise" : { "BOOL" : [true, false] } } }
          },
          {
            "eventName" : ["MODIFY"],
            "dynamodb" : { "NewImage" : { "spidMinors" : { "BOOL" : [true, false] } } }
          },
          {
            "eventName" : ["MODIFY"],
            "dynamodb" : { "NewImage" : { "spidProfessionals" : { "BOOL" : [true, false] } } }
          },
          {
            "eventName" : ["MODIFY"],
            "dynamodb" : { "NewImage" : { "samlBinding" : { "S" : [{ "prefix" : "" }] } } }
          },
          {
            "eventName" : ["MODIFY"],
            "dynamodb" : { "NewImage" : { "authLevel" : { "S" : [{ "prefix" : "" }] } } }
          }
        ])
      }
      filter {
        pattern = jsonencode({ "eventName" : ["REMOVE"] })
      }
    }
  }
}
