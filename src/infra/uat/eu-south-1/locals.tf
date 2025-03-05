locals {
  project            = format("%s-%s-%s", var.app_name, var.aws_region_short, var.env_short)
  ecr_oneid_core     = format("%s-core", local.project)
  ecr_spid_validator = format("%s-spid-validator", local.project)

  cloudwatch__ecs_alarms_with_sns = {
    for key, alarm in var.ecs_alarms : key => merge(
      alarm,
      {
        sns_topic_alarm_arn = module.sns.sns_topic_arn
      }
    )
  }

  cloudwatch__lambda_alarms_with_sns = {
    for key, alarm in var.lambda_alarms : key => merge(
      alarm,
      {
        sns_topic_alarm_arn = module.sns.sns_topic_arn
      }
    )
  }

  cloudwatch__dlq_alarms_with_sns = merge(var.dlq_alarms, {
    sns_topic_alarm_arn = module.sns.sns_topic_arn
  })

  cloudwatch__api_alarms_with_sns = {
    for key, alarm in var.api_alarms : key => merge(
      alarm,
      {
        sns_topic_alarm_arn = module.sns.sns_topic_arn
      }
    )
  }

  idp_entity_ids = try(
    [for entity in jsondecode(data.http.idps_api.response_body) : entity.entityID],
    []
  )

  clients = try(
    [for client in jsondecode(data.http.clients_api.response_body) : {
      clientID     = client.clientID
      friendlyName = client.friendlyName
    }],
    []
  )
}

data "http" "idps_api" {
  url = "https://uat.oneid.pagopa.it/idps"
  retry {
    attempts     = 3
    min_delay_ms = 1000
  }

  lifecycle {
    postcondition {
      condition     = self.status_code == 200
      error_message = "Status code invalid"
    }
  }
}

data "http" "clients_api" {
  url = "https://uat.oneid.pagopa.it/clients"
  retry {
    attempts     = 3
    min_delay_ms = 1000
  }

  lifecycle {
    postcondition {
      condition     = self.status_code == 200
      error_message = "Status code invalid"
    }
  }
}

