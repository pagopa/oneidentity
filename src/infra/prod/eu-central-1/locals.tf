locals {
  project            = format("%s-%s-%s", var.app_name, var.aws_region_short, var.env_short)
  ecr_oneid_core     = format("%s-core", local.project)
  ecr_spid_validator = format("%s-spid-validator", local.project)

  ## Arn with string concatenation since the table is a replica of the existing one in the main region.
  table_client_registrations_arn = "arn:aws:dynamodb:${var.aws_region}:${data.aws_caller_identity.current.account_id}:table/ClientRegistrations"

  table_idpMetadata_gsi_pointer_arn = "arn:aws:dynamodb:${var.aws_region}:${data.aws_caller_identity.current.account_id}:table/IDPMetadata/index/gsi_pointer_idx"
  table_idp_metadata_arn            = "arn:aws:dynamodb:${var.aws_region}:${data.aws_caller_identity.current.account_id}:table/IDPMetadata"

  cloudwatch_ecs_alarms_with_sns = {
    for key, alarm in var.ecs_alarms : key => merge(
      alarm,
      {
        sns_topic_alarm_arn = module.sns.sns_topic_arn
      }
    )
  }

  cloudwatch_lambda_alarms_with_sns = {
    for key, alarm in var.lambda_alarms : key => merge(
      alarm,
      {
        sns_topic_alarm_arn = module.sns.sns_topic_arn
      }
    )
  }

  cloudwatch_dlq_alarms_with_sns = merge(var.dlq_alarms, {

    sns_topic_alarm_arn = module.sns.sns_topic_arn

  })

  cloudwatch_api_alarms_with_sns = {
    for key, alarm in var.api_alarms : key => merge(
      alarm,
      {
        sns_topic_alarm_arn = module.sns.sns_topic_arn
      }
    )
  }

  idp_entity_ids = concat(
    try(
    [for entity in jsondecode(data.http.idps_api.response_body) : entity.entityID], []),
    ["https://idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO"]
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
  url = "https://oneid.pagopa.it/idps"
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
  url = "https://oneid.pagopa.it/clients"
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
