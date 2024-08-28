data "aws_ssm_parameter" "subscribers" {
  name = var.ssm_parameter_name
}

locals {
  emails = split(",", data.aws_ssm_parameter.subscribers.value)
}

resource "aws_sns_topic" "alarms" {
  name         =  var.sns_topic_name
  display_name = "Alarms"
}

resource "aws_sns_topic_subscription" "alarms_email" {
  count                  = length(local.emails)
  endpoint               = local.emails[count.index] 
  endpoint_auto_confirms = true
  protocol               = "email"
  topic_arn              = aws_sns_topic.alarms.arn
}
