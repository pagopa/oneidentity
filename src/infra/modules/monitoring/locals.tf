locals {
  idp_widgets = [for entity_id in var.idp_entity_ids.entity_id : templatefile("../../dashboards/idp_error_widget.tpl.json", {
    aws_region = var.aws_region
    idp        = entity_id
    env_short  = var.env_short
  })]

  client_widgets = [for client in var.client_ids.client_ids : templatefile("../../dashboards/client_error_widget.tpl.json", {
    aws_region = var.aws_region
    client_id  = client
    env_short  = var.env_short
  })]

  aggregated_success_widget = templatefile("../../dashboards/client_aggregated_widget.tpl.json", {
    aggregated_metric = "ClientSuccess",
    aws_region = var.aws_region
    env_short  = var.env_short
  })  

  aggregated_error_widget = templatefile("../../dashboards/client_aggregated_widget.tpl.json", {
    aggregated_metric = "ClientError",
    aws_region = var.aws_region
    env_short  = var.env_short
  })

  idp_widget_header = {
    "height" : 1,
    "width" : 24,
    "type" : "text",
    "properties" : {
      "markdown" : "## IDP Errors\n"
    }
  }

  client_widget_header = {
    "height" : 1,
    "width" : 24,
    "type" : "text",
    "properties" : {
      "markdown" : "## Client Errors\n"
    }
  }

  client_aggregated_widget_header = {
    "height" : 1,
    "width" : 24,
    "type" : "text",
    "properties" : {
      "markdown" : "## Client Aggregated Success/Errors\n"
    }
  }

  detailed_metrics_dashboard_body = jsonencode({
    widgets = concat(
      [local.idp_widget_header],
      [for w in local.idp_widgets : jsondecode(w)],
      [local.client_widget_header],
      [for w in local.client_widgets : jsondecode(w)],
      [local.client_aggregated_widget_header],
      [jsondecode(local.aggregated_success_widget)],
      [jsondecode(local.aggregated_error_widget)]
    )
    }
  )

}
