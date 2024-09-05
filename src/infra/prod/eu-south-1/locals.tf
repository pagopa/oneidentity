locals {
  project        = format("%s-%s-%s", var.app_name, var.aws_region_short, var.env_short)
  container_name = "oneidentity"
  alb_name       = format("%s-alb", local.project)
  ecr_core       = format("%s-core-ecr", local.project)
  ecr_oneid_core = format("%s-core", local.project)
}



locals {
  cloudwatch__ecs_alarms_with_sns = {
    for key, alarm in var.ecs_alarms : key => merge(
      alarm,
      {
        sns_topic_alarm_arn = module.sns.sns_topic_arn
      }
    )
  }

  cloudwatch__lambda_alarms_with_sns = merge(var.lambda_alarms, {

    sns_topic_alarm_arn = module.sns.sns_topic_arn

  })

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
