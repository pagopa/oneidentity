locals {
  project            = format("%s-%s-%s", var.app_name, var.aws_region_short, var.env_short)
  ecr_oneid_core     = format("%s-core", local.project)
  ecr_spid_validator = format("%s-spid-validator", local.project)

  ## Arn with string concatenation since the table is a replica of the existing one in the main region.
  table_client_registrations_arn = "arn:aws:dynamodb:${var.aws_region}:${data.aws_caller_identity.current.account_id}:table/ClientRegistrations"

  table_idpMetadata_gsi_pointer_arn = "arn:aws:dynamodb:${var.aws_region}:${data.aws_caller_identity.current.account_id}:table/IDPMetadata/index/gsi_pointer_idx"
  table_idp_metadata_arn            = "arn:aws:dynamodb:${var.aws_region}:${data.aws_caller_identity.current.account_id}:table/IDPMetadata"

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
}
