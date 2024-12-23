locals {
  query_files = fileset("../../cloudwatch-query", "*.sql") # Get all .sql files from the directory
}

data "aws_ssm_parameter" "alarm_subscribers" {
  name = var.alarm_subscribers
}

resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.main_dashboard_name

  dashboard_body = templatefile("../../dashboards/main.tpl.json",
    {
      aws_region                 = var.aws_region
      api_name                   = var.api_name
      sessions_table             = var.sessions_table
      client_registrations_table = var.client_registrations_table
      ecs                        = var.ecs
      nlb                        = var.nlb

    }
  )
}

resource "aws_cloudwatch_dashboard" "api_methods" {
  dashboard_name = var.api_methods_dashboard_name

  dashboard_body = templatefile("../../dashboards/api_methods.tpl.json",
    {
      aws_region = var.aws_region
      api_name   = var.api_name
    }
  )
}

resource "aws_cloudwatch_query_definition" "ecs_log_level_error" {
  for_each = toset(local.query_files)

  name = "ECS/${replace(each.value, ".sql", "")}" # Dynamically set the name based on the query file
  log_group_names = [
    var.ecs.log_group_name,
  ]
  query_string = file("../../cloudwatch-query/${each.value}") # Read the query from the file dynamically
}

resource "aws_ce_anomaly_monitor" "service_monitor" {
  name              = "AWSServiceMonitor"
  monitor_type      = "DIMENSIONAL"
  monitor_dimension = "SERVICE"
}

resource "aws_ce_anomaly_subscription" "main" {
  name      = "DAILYSUBSCRIPTION"
  frequency = "DAILY"

  monitor_arn_list = [
    aws_ce_anomaly_monitor.service_monitor.arn,
  ]

  subscriber {
    type    = "EMAIL"
    address = data.aws_ssm_parameter.alarm_subscribers.value
  }

  threshold_expression {
    dimension {
      key           = "ANOMALY_TOTAL_IMPACT_ABSOLUTE"
      values        = ["300.0"]
      match_options = ["GREATER_THAN_OR_EQUAL"]
    }
  }
}