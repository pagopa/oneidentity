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
