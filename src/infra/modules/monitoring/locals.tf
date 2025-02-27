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
}
