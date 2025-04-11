locals {
  idp_widgets = [
    for entity_id in var.idp_entity_ids :
    templatefile("../../dashboards/idp_error_widget.tpl.json", {
      aws_region   = var.aws_region
      idp          = entity_id
      env_short    = var.env_short
      region_short = var.region_short
    })
  ]

  client_widgets = [
    for client in var.clients : templatefile("../../dashboards/client_error_widget.tpl.json", {
      aws_region    = var.aws_region
      client_id     = client.client_id
      friendly_name = client.friendly_name
      env_short     = var.env_short
      region_short  = var.region_short

    })
  ]

  client_aggregated_widgets = [
    for client in var.clients : templatefile("../../dashboards/client_aggregated_widget.tpl.json", {
      client_id     = client.client_id
      friendly_name = client.friendly_name
      aws_region    = var.aws_region
      env_short     = var.env_short
      region_short  = var.region_short

    })
  ]

  samlstatus_idp_widgets = [
    for entity_id in var.idp_entity_ids :
    templatefile("../../dashboards/samlstatus_idp_related_error_widget.tpl.json", {
      idp          = entity_id
      aws_region   = var.aws_region
      env_short    = var.env_short
      region_short = var.region_short

    })
  ]

  samlstatus_client_widgets = [
    for client in var.clients :
    templatefile("../../dashboards/samlstatus_client_related_error_widget.tpl.json", {
      client_id     = client.client_id
      friendly_name = client.friendly_name
      aws_region    = var.aws_region
      env_short     = var.env_short
      region_short  = var.region_short

    })
  ]

  assertion_counter_widgets = [
    templatefile("../../dashboards/assertion_counter_widget.tpl.json", {
      aws_region = var.aws_region
    })
  ]

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

  samlstatus_idp_error_widget_header = {
    "height" : 1,
    "width" : 24,
    "type" : "text",
    "properties" : {
      "markdown" : "## SAML Status Errors - by IDP\n"
    }
  }

  samlstatus_client_error_widget_header = {
    "height" : 1,
    "width" : 24,
    "type" : "text",
    "properties" : {
      "markdown" : "## SAML Status Errors - by Client\n"
    }
  }

  assertion_counter_widget_header = {
    "height" : 1,
    "width" : 24,
    "type" : "text",
    "properties" : {
      "markdown" : "## Assertions Counter\n"
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
      [local.samlstatus_idp_error_widget_header],
      [for w in local.samlstatus_idp_widgets : jsondecode(w)],
      [local.samlstatus_client_error_widget_header],
      [for w in local.samlstatus_client_widgets : jsondecode(w)],
      [local.assertion_counter_widget_header],
      [for w in local.assertion_counter_widgets : jsondecode(w)]
    )
    }
  )

}
