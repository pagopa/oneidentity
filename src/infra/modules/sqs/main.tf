# SQS queue for storing PDV failure calls to be retried later
resource "aws_sqs_queue" "pdv_errors_queue" {
  name                    = var.sqs_queue_name
  sqs_managed_sse_enabled = true
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.pdv_errors_deadletter_queue.arn
    maxReceiveCount     = 4
  })
}

# Dead Letter Queue for the SQS queue
resource "aws_sqs_queue" "pdv_errors_deadletter_queue" {
  name                    = "${var.sqs_queue_name}-dlq"
  sqs_managed_sse_enabled = true
}

resource "aws_sqs_queue_redrive_allow_policy" "pdv_errors_queue_redrive_allow_policy" {
  queue_url = aws_sqs_queue.pdv_errors_deadletter_queue.id

  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue",
    sourceQueueArns   = [aws_sqs_queue.pdv_errors_queue.arn]
  })
}

# Alarm for monitoring errors during SQS message sending
resource "aws_cloudwatch_metric_alarm" "sqs_error_sending_message_alarm" {
  alarm_name          = format("%s_%s_%s", "SQSErrorsSendingMessageAlarm", var.env_short, var.region_short)
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  threshold           = 1
  metric_name         = "OIError"
  namespace           = "oneid-${var.region_short}-${var.env_short}-core/ApplicationMetrics"
  period              = "60"
  statistic           = "Sum"

  dimensions = {
    OIError = "SQSSendMessageFailures"
  }
  alarm_actions = [
    var.sns_topic_arn,
  ]
}

# Alarm for monitoring the DLQ of the PDV errors SQS queue
resource "aws_cloudwatch_metric_alarm" "dlq_pdv_errors" {
  alarm_name = format("%s-%s-Dlq-%s", var.sqs_queue_name, var.dlq_alarms.metric_name,
  var.dlq_alarms.threshold)
  comparison_operator = var.dlq_alarms.comparison_operator
  evaluation_periods  = var.dlq_alarms.evaluation_periods
  metric_name         = var.dlq_alarms.metric_name
  namespace           = var.dlq_alarms.namespace
  period              = var.dlq_alarms.period
  statistic           = var.dlq_alarms.statistic
  threshold           = var.dlq_alarms.threshold


  dimensions = {
    QueueName = aws_sqs_queue.pdv_errors_deadletter_queue.name
  }

  alarm_actions = [var.dlq_alarms.sns_topic_alarm_arn]
}