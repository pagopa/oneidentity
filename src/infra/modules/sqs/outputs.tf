output "sqs_queue_arn" {
  value = aws_sqs_queue.pdv_errors_queue.arn
}

output "kms_sqs_queue_alias_arn" {
  value = module.kms_sqs_queue.aliases[local.kms_sqs_queue_alias].target_key_arn
}