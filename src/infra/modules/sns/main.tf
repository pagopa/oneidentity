data "aws_ssm_parameter" "alarm_subscribers" {
  name = var.alarm_subscribers
}

locals {
  emails = split(",", data.aws_ssm_parameter.alarm_subscribers.value)
}

resource "aws_sns_topic" "alarms" {
  name         = var.sns_topic_name
  display_name = "Alarms"
}

data "aws_iam_policy_document" "alarms" {
  policy_id = "__default_policy_ID"

  statement {
    sid    = "__default_statement_ID"
    effect = "Allow"

    principals {
      type        = "AWS"
      identifiers = ["*"]
    }

    actions = [
      "SNS:GetTopicAttributes",
      "SNS:SetTopicAttributes",
      "SNS:AddPermission",
      "SNS:RemovePermission",
      "SNS:DeleteTopic",
      "SNS:Subscribe",
      "SNS:ListSubscriptionsByTopic",
      "SNS:Publish",
    ]

    resources = [aws_sns_topic.alarms.arn]

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceOwner"
      values   = [var.account_id]
    }
  }

  statement {
    sid    = "AllowEcsDeploymentFailedEvents"
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["events.amazonaws.com"]
    }

    actions   = ["SNS:Publish"]
    resources = [aws_sns_topic.alarms.arn]

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = ["arn:aws:events:${var.aws_region}:${var.account_id}:rule/${var.ecs_deployment_failed_rule_name}"]
    }
  }
}

resource "aws_sns_topic_policy" "alarms" {
  arn    = aws_sns_topic.alarms.arn
  policy = data.aws_iam_policy_document.alarms.json
}

resource "aws_sns_topic_subscription" "alarms_email" {
  count                  = length(local.emails)
  endpoint               = local.emails[count.index]
  endpoint_auto_confirms = true
  protocol               = "email"
  topic_arn              = aws_sns_topic.alarms.arn
}
