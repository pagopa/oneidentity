resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.main_dashboard_name

  dashboard_body = templatefile("../dashboards/main.tpl.json",
    {
      aws_region          = var.aws_region
      api_name            = var.api_name
      dynamodb_table_name = var.dynamodb_table_name
      ecs                 = var.ecs
      nlb                 = var.nlb

    }
  )
}