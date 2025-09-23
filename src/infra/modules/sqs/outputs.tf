output "sqs_queue_arn" {
  value = aws_sqs_queue.pdv_errors_queue.arn
}

output "sqs_queue_url" {
  value = aws_sqs_queue.pdv_errors_queue.url
}