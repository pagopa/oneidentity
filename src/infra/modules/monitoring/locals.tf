locals {
  idp_widgets = [for entity_id in var.idp_entity_ids : templatefile("../../dashboards/idp_error_widget.tpl.json", {
    aws_region = var.aws_region
    idp        = entity_id
    env_short  = var.env_short
  })]

  client_widgets = [for client in var.clients : templatefile("../../dashboards/client_error_widget.tpl.json", {
    aws_region    = var.aws_region
    client_id     = client.client_id
    friendly_name = client.friendly_name
    env_short     = var.env_short
  })]

  client_aggregated_widgets = [for client in var.clients : templatefile("../../dashboards/client_aggregated_widget.tpl.json", {
    client_id     = client.client_id
    friendly_name = client.friendly_name
    aws_region    = var.aws_region
    env_short     = var.env_short
  })]

  user_idp_widgets = [for error_code in var.spid_error_codes : templatefile("../../dashboards/user_idp_related_error_widget.tpl.json", {
    error_code = error_code
    aws_region = var.aws_region
    env_short  = var.env_short
  })]

  user_client_widgets = [for error_code in var.spid_error_codes : templatefile("../../dashboards/user_client_related_error_widget.tpl.json", {
    error_code = error_code
    aws_region = var.aws_region
    env_short  = var.env_short
  })]

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

  user_idp_error_widget_header = {
    "height" : 1,
    "width" : 24,
    "type" : "text",
    "properties" : {
      "markdown" : "## IDP related users errors\n"
    }
  }

  user_client_error_widget_header = {
    "height" : 1,
    "width" : 24,
    "type" : "text",
    "properties" : {
      "markdown" : "## Client related users errors\n"
    }
  }

  detailed_metrics_dashboard_body = jsonencode({
    widgets = concat(
      [local.idp_widget_header],
      [for w in local.idp_widgets : jsondecode(w)],
      [local.client_widget_header],
      [for w in local.client_widgets : jsondecode(w)],
      [local.client_aggregated_widget_header],
      [for w in local.client_aggregated_widgets : jsondecode(w)],
      [local.user_idp_error_widget_header],
      [for w in local.user_idp_widgets : jsondecode(w)],
      [local.user_client_error_widget_header],
      [for w in local.user_client_widgets : jsondecode(w)]
    )
    }
  )

}
