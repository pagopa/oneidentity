output "sns_topic_arn" {
  description = "ARN of the alarms SNS topic."
  value       = aws_sns_topic.alarms.arn
  depends_on  = [aws_sns_topic_policy.alarms]
}