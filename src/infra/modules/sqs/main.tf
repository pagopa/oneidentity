locals {
  kms_sqs_queue_alias = "/sqs/pdv-errors"
}

# Sessions
module "kms_sqs_queue" {
  source  = "terraform-aws-modules/kms/aws"
  version = "3.0.0"

  description             = "KMS key for SQS queue messages encryption."
  key_usage               = "ENCRYPT_DECRYPT"
  enable_key_rotation     = var.kms_ssm_enable_rotation
  rotation_period_in_days = var.kms_rotation_period_in_days

  # Aliases
  aliases = [local.kms_sqs_queue_alias]
}
# SQS queue for storing PDV failure calls to be retried later
resource "aws_sqs_queue" "pdv_errors_queue" {
  name              = var.sqs_queue_name
  kms_master_key_id = local.kms_sqs_queue_alias
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.pdv_errors_deadletter_queue.arn
    maxReceiveCount     = 4
  })
}

# Dead Letter Queue for the SQS queue
resource "aws_sqs_queue" "pdv_errors_deadletter_queue" {
  name              = "${var.sqs_queue_name}-dlq"
  kms_master_key_id = local.kms_sqs_queue_alias
}

resource "aws_sqs_queue_redrive_allow_policy" "pdv_errors_queue_redrive_allow_policy" {
  queue_url = aws_sqs_queue.pdv_errors_deadletter_queue.id

  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue",
    sourceQueueArns   = [aws_sqs_queue.pdv_errors_queue.arn]
  })
}