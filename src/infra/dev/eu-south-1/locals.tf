locals {
  project                = format("%s-%s-%s", var.app_name, var.aws_region_short, var.env_short)
  ecr_oneid_core         = format("%s-core", local.project)
  ecr_oneid_internal_idp = format("%s-internal-idp", local.project)
  ecr_spid_validator     = format("%s-spid-validator", local.project)

  cloudwatch_ecs_alarms_without_sns = var.ecs_alarms

  cloudwatch_lambda_alarms_without_sns = var.lambda_alarms

  cloudwatch_dlq_alarms_without_sns = var.dlq_alarms

  cloudwatch_api_alarms_without_sns = var.api_alarms

  idp_entity_ids = concat(
    try(
    [for entity in jsondecode(data.http.idps_api.response_body) : entity.entityID], []),
    [
      "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO",
      "https://collaudo.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO"
    ]
  )

  clients = try(
    [for client in jsondecode(data.http.clients_api.response_body) : {
      client_id     = client.clientID
      friendly_name = client.friendlyName
    }],
    []
  )
}

data "http" "idps_api" {
  url = "https://dev.oneid.pagopa.it/idps"
  retry {
    attempts     = 3
    min_delay_ms = 1000
  }

  lifecycle {
    postcondition {
      condition     = self.status_code == 200
      error_message = "Status code invalid"
    }
    postcondition {
      condition     = alltrue([for idp in jsondecode(self.response_body) : can(idp.entityID)])
      error_message = "Each idp must include 'entityID'"
    }
  }
}

data "http" "clients_api" {
  url = "https://dev.oneid.pagopa.it/clients"
  retry {
    attempts     = 3
    min_delay_ms = 1000
  }

  lifecycle {
    postcondition {
      condition     = self.status_code == 200
      error_message = "Status code invalid"
    }
    postcondition {
      condition     = alltrue([for client in jsondecode(self.response_body) : can(client.clientID) && can(client.friendlyName)])
      error_message = "Each Client must include 'clientID' and 'friendlyName'"
    }
  }
}
