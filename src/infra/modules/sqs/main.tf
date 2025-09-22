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