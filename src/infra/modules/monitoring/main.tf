locals {
  query_files = fileset("../../cloudwatch-query", "*.sql") # Get all .sql files from the directory
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