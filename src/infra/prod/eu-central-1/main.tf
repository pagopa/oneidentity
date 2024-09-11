data "aws_caller_identity" "current" {}

module "network" {
  source   = "../../modules/network"
  vpc_name = format("%s-vpc", local.project)

  azs = ["eu-central-1a", "eu-central-1b", "eu-central-1c"]

  vpc_cidr                  = var.vpc_cidr
  vpc_private_subnets_cidr  = var.vpc_private_subnets_cidr
  vpc_public_subnets_cidr   = var.vpc_public_subnets_cidr
  vpc_internal_subnets_cidr = var.vpc_internal_subnets_cidr
  enable_nat_gateway        = var.enable_nat_gateway
  single_nat_gateway        = var.single_nat_gateway
}

## SNS for alarms ##
module "sns" {
  source            = "../../modules/sns"
  sns_topic_name    = format("%s-sns", local.project)
  alarm_subscribers = var.alarm_subscribers
}


module "storage" {
  source = "../../modules/storage"

  assertion_bucket = {
    name_prefix               = "assertions"
    glacier_transaction_days  = var.assertion_bucket.glacier_transaction_days
    expiration_days           = var.assertion_bucket.expiration_days
    enable_key_rotation       = var.assertion_bucket.enable_key_rotation
    kms_multi_region          = var.assertion_bucket.kms_multi_region
    object_lock_configuration = var.assertion_bucket.object_lock_configuration
  }
  create_athena_table         = false
  assertions_crawler_schedule = var.assertions_crawler_schedule

  assets_bucket_prefix = "assets"
  github_repository    = "pagopa/oneidentity"
  account_id           = data.aws_caller_identity.current.account_id
}

/*
## Database ##  
module "database" {
  source                     = "../../modules/database"
  sessions_table             = var.sessions_table
  client_registrations_table = var.client_registrations_table
}

## Backend ##

module "backend" {
  source = "../../modules/backend"

  aws_region = var.aws_region

  ecr_registers = [
    {
      name                            = local.ecr_oneid_core
      number_of_images_to_keep        = var.number_of_images_to_keep
      repository_image_tag_mutability = var.repository_image_tag_mutability
    }
  ]

  ecs_cluster_name          = format("%s-ecs", local.project)
  enable_container_insights = var.ecs_enable_container_insights

  fargate_capacity_providers = {
    FARGATE = {
      default_capacity_provider_strategy = {
        weight = 50
        base   = 20
      }
    }
  }

  sns_topic_arn   = module.sns.sns_topic_arn
  ecs_alarms      = local.cloudwatch__ecs_alarms_with_sns
  lambda_alarms   = local.cloudwatch__lambda_alarms_with_sns
  dlq_alarms      = local.cloudwatch__dlq_alarms_with_sns
  vpc_id          = module.network.vpc_id
  private_subnets = module.network.private_subnet_ids
  vpc_cidr_block  = module.network.vpc_cidr_block

  service_core = {
    service_name = format("%s-core", local.project)

    cpu    = var.ecs_oneid_core.cpu
    memory = var.ecs_oneid_core.memory

    container = {
      name                = "oneid-core"
      cpu                 = var.ecs_oneid_core.container_cpu
      memory              = var.ecs_oneid_core.container_memory
      image_name          = local.ecr_oneid_core
      image_version       = var.ecs_oneid_core.image_version
      containerPort       = 8080
      hostPort            = 8080
      logs_retention_days = var.ecs_oneid_core.logs_retention_days
    }

    autoscaling = var.ecs_oneid_core.autoscaling

    subnet_ids = module.network.private_subnet_ids

    environment_variables = [
      {
        name  = "METADATA_URL",
        value = "https://${var.r53_dns_zone.name}/saml/metadata"
      },
      {
        name  = "SERVICE_PROVIDER_URI"
        value = "https://${var.r53_dns_zone.name}"
      },
      {
        name  = "ACS_URL"
        value = "https://${var.r53_dns_zone.name}/saml/acs"
      }
    ]
  }

  ## NLB ##
  nlb_name = format("%s-nlb", local.project)

  github_repository = "pagopa/oneidentity"
  account_id        = data.aws_caller_identity.current.account_id

  dynamodb_table_sessions = {
    table_arn    = module.database.table_sessions_arn
    gsi_code_arn = module.database.table_sessions_gsi_code_arn
  }

  table_client_registrations_arn = module.database.table_client_registrations_arn

  kms_sessions_table_alias_arn = module.database.kms_sessions_table_alias_arn

  client_registration_lambda = {
    name                              = format("%s-client-registration", local.project)
    filename                          = "${path.module}/../../hello-java/build/libs/hello-java-1.0-SNAPSHOT.jar"
    table_client_registrations_arn    = module.database.table_client_registrations_arn
    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
    vpc_id                            = module.network.vpc_id
    vpc_subnet_ids                    = module.network.intra_subnets_ids
    vpc_endpoint_dynamodb_prefix_id   = module.network.vpc_endpoints["dynamodb"]["prefix_list_id"]
  }

  metadata_lambda = {
    name                           = format("%s-metadata", local.project)
    filename                       = "${path.module}/../../hello-java/build/libs/hello-java-1.0-SNAPSHOT.jar"
    table_client_registrations_arn = module.database.table_client_registrations_arn
    environment_variables = {
      "ORGANIZATION_URL"                = "https://www.pagopa.it"
      "CONTACT_PERSON_EMAIL_ADDRESS"    = "pagopa@pec.governo.it"
      "ORGANIZATION_DISPLAY_NAME"       = "PagoPA S.p.A."
      "METADATA_URL"                    = "https://${var.r53_dns_zone.name}/saml/metadata"
      "SERVICE_PROVIDER_URI"            = "https://${var.r53_dns_zone.name}"
      "ORGANIZATION_NAME"               = "PagoPA S.p.A."
      "ACS_URL"                         = "https://${var.r53_dns_zone.name}/saml/acs"
      "SLO_URL"                         = "https://${var.r53_dns_zone.name}/saml/slo"
      "CONTACT_PERSON_COMPANY"          = "PagoPA S.p.A."
      "CLIENT_REGISTRATIONS_TABLE_NAME" = "ClientRegistrations"
    }
    vpc_id                          = module.network.vpc_id
    vpc_subnet_ids                  = module.network.intra_subnets_ids
    vpc_endpoint_dynamodb_prefix_id = module.network.vpc_endpoints["dynamodb"]["prefix_list_id"]

    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
  }


  dynamodb_table_stream_arn = module.database.dynamodb_table_stream_arn
  eventbridge_pipe_sessions = {
    pipe_name                     = format("%s-sessions-pipe", local.project)
    kms_sessions_table_alias      = module.database.kms_sessions_table_alias_arn
    maximum_retry_attempts        = var.dlq_assertion_setting.maximum_retry_attempts
    maximum_record_age_in_seconds = var.dlq_assertion_setting.maximum_record_age_in_seconds
  }

  assertion_lambda = {
    name                    = format("%s-assertion", local.project)
    filename                = "${path.module}/../../hello-python/lambda.zip"
    s3_assertion_bucket_arn = module.storage.assertions_bucket_arn
    kms_assertion_key_arn   = module.storage.kms_assertion_key_arn

    environment_variables = {
      S3_BUCKET = module.storage.assertions_bucket_name
    }
    vpc_id                            = module.network.vpc_id
    vpc_subnet_ids                    = module.network.intra_subnets_ids
    vpc_s3_prefix_id                  = module.network.vpc_endpoints["s3"]["prefix_list_id"]
    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
  }
}

module "frontend" {
  source = "../../modules/frontend"

  ## DNS
  domain_name     = module.r53_zones.dns_zone_name
  r53_dns_zone_id = module.r53_zones.dns_zone_id

  ## API Gateway ##
  rest_api_name         = format("%s-restapi", local.project)
  openapi_template_file = "../../api/oi.tpl.json"

  dns_record_ttl = var.dns_record_ttl

  api_gateway_target_arns = [module.backend.nlb_arn]
  nlb_dns_name            = module.backend.nlb_dns_name

  api_gateway_plan = {
    name                 = format("%s-restapi-plan", local.project)
    throttle_burst_limit = var.rest_api_throttle_settings.burst_limit
    throttle_rate_limit  = var.rest_api_throttle_settings.rate_limit
    api_key_name         = "client-registration"
  }

  metadata_lamba_name            = module.backend.metadata_lambda_name
  metadata_lamba_arn             = module.backend.metadata_lambda_arn
  client_registration_lambda_arn = module.backend.client_registration_lambda_arn
  aws_region                     = var.aws_region
  assets_bucket_arn              = module.storage.assets_bucket_arn
  assets_bucket_name             = module.storage.assets_bucket_name
  api_cache_cluster_enabled      = var.api_cache_cluster_enabled
  api_method_settings            = var.api_method_settings

  xray_tracing_enabled = var.xray_tracing_enabled
  api_alarms           = local.cloudwatch__api_alarms_with_sns
}

## Monitoring / Dashboard ##

module "monitoring" {
  source                     = "../../modules/monitoring"
  main_dashboard_name        = format("%s-overall-dashboard", local.project)
  api_methods_dashboard_name = format("%s-api-methods-dashboard", local.project)
  aws_region                 = var.aws_region
  api_name                   = module.frontend.api_name
  sessions_table             = module.database.table_sessions_name
  client_registrations_table = module.database.table_client_registrations_name
  nlb = {
    target_group_arn_suffix = module.backend.nlb_target_group_suffix_arn
    arn_suffix              = module.backend.nlb_arn_suffix
  }
  ecs = {
    service_name = module.backend.ecs_service_name,
    cluster_name = module.backend.ecs_cluster_name
  }
}

*/