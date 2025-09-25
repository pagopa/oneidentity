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

module "sqs" {
  source         = "../../modules/sqs"
  sqs_queue_name = format("%s-pdv-reconciler-sqs", local.project)
  sns_topic_arn  = module.sns.sns_topic_arn
  env_short      = var.env_short
  region_short   = var.aws_region_short
}

module "storage" {
  source = "../../modules/storage"

  role_prefix = local.project

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

  idp_metadata_bucket_prefix         = "idp-metadata"
  assets_bucket_prefix               = "assets"
  assets_bucket_control_panel_prefix = "assets-control-panel"
  github_repository                  = "pagopa/oneidentity"
  account_id                         = data.aws_caller_identity.current.account_id
  assertion_accesslogs_expiration    = 180
}

## Database ##  
module "database" {
  source         = "../../modules/database"
  sessions_table = var.sessions_table
  // the following tables won't be created in the secondary region since 
  // they are replicated from the primary region.
  client_registrations_table  = null
  idp_metadata_table          = null
  idp_status_history_table    = var.idp_status_history_table
  client_status_history_table = var.client_status_history_table
  last_idp_used_table         = var.last_idp_used_table
  // the following table should not be created in the production environment
  internal_idp_users_table = null
  internal_idp_sessions    = null
  idp_entity_ids           = local.idp_entity_ids
  clients                  = local.clients
}


## Backend ##
module "backend" {
  source = "../../modules/backend"

  aws_region = var.aws_region
  env_short  = var.env_short

  role_prefix = local.project

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
        name  = "BASE_PATH",
        value = "https://${var.r53_dns_zone.name}"
      },
      {
        name  = "ENTITY_ID",
        value = "https://${var.r53_dns_zone.name}/pub-op-full"
      },
      {
        name  = "ACS_URL"
        value = var.metadata_info.acs_url
      },
      {
        name  = "TIMESTAMP_SPID"
        value = "LATEST_SPID"
      },
      {
        name  = "TIMESTAMP_CIE"
        value = "LATEST_CIE"
      },
      {
        name  = "CIE_ENTITY_ID"
        value = var.cie_entity_id
      },
      {
        name  = "CERTIFICATE_NAME"
        value = var.ssm_cert_key.cert_pem
      },
      {
        name  = "KEY_NAME"
        value = var.ssm_cert_key.key_pem
      },
      {
        name  = "LOG_LEVEL"
        value = var.app_log_level
      },
      {
        name  = "CLOUDWATCH_CUSTOM_METRIC_NAMESPACE"
        value = format("%s/%s", format("%s-core", local.project), var.app_cloudwatch_custom_metric_namespace)
      },
      {
        name  = "PDV_BASE_URL"
        value = "https://api.pdv.pagopa.it"
      },
      {
        name  = "PDV_ERROR_QUEUE_URL"
        value = module.sqs.sqs_queue_url
      },
      {
        name  = "PAIRWISE_ENABLED"
        value = var.pairwise_enabled
      }
    ]
  }

  service_internal_idp = null

  ssm_idp_internal_cert_key = {}

  ssm_cert_key = var.ssm_cert_key

  eventbridge_pipe_invalidate_cache = {
    pipe_name                     = format("%s-flush-apigw-cache-pipe", local.project)
    maximum_retry_attempts        = var.dlq_assertion_setting.maximum_retry_attempts
    maximum_record_age_in_seconds = var.dlq_assertion_setting.maximum_record_age_in_seconds
  }

  invalidate_cache_lambda = {
    name     = format("%s-invalidate-cache", local.project)
    filename = "${path.module}/../../hello-python/lambda.zip"
    #vpc_endpoint_dynamodb_prefix_id   = module.network.vpc_endpoints["dynamodb"]["prefix_list_id"]
    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
    rest_api_execution_arn            = module.frontend.rest_api_execution_arn
    rest_api_arn                      = module.frontend.rest_api_arn
    environment_variables = {
      REST_API_ID = module.frontend.rest_api_id
      STAGE_NAME  = module.frontend.rest_api_stage_name
    }
  }

  client_manager_lambda_optional_iam_policy = false

  client_manager_lambda = {
    name                              = format("%s-client-manager", local.project)
    filename                          = "${path.module}/../../hello-python/lambda.zip"
    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
    table_client_registrations_arn    = module.database.table_client_registrations_arn
  }

  pdv_reconciler_lambda = {
    name                               = format("%s-pdv-reconciler", local.project)
    filename                           = "${path.module}/../../hello-python/lambda.zip"
    cloudwatch_logs_retention_in_days  = var.lambda_cloudwatch_logs_retention_in_days
    vpc_id                             = module.network.vpc_id
    vpc_subnet_ids                     = module.network.intra_subnets_ids
    vpc_tls_security_group_endpoint_id = module.network.security_group_vpc_tls_id
    pdv_errors_queue_arn               = module.sqs.sqs_queue_arn
    environment_variables = {
      "LOG_LEVEL" = var.app_log_level
    }
  }

  ## NLB ##
  nlb_name = format("%s-nlb", local.project)

  github_repository = "pagopa/oneidentity"
  account_id        = data.aws_caller_identity.current.account_id

  dynamodb_table_sessions = {
    table_arn    = module.database.table_sessions_arn
    gsi_code_arn = module.database.table_sessions_gsi_code_arn
  }

  table_client_registrations_arn = local.table_client_registrations_arn

  kms_sessions_table_alias_arn = module.database.kms_sessions_table_alias_arn

  client_registration_lambda = {
    name     = format("%s-client-registration", local.project)
    filename = "${path.module}/../../hello-java/build/libs/hello-java-1.0-SNAPSHOT.jar"
    // todo this must be the replica arn
    table_client_registrations_arn     = local.table_client_registrations_arn
    cloudwatch_logs_retention_in_days  = var.lambda_cloudwatch_logs_retention_in_days
    vpc_id                             = module.network.vpc_id
    vpc_subnet_ids                     = module.network.intra_subnets_ids
    vpc_tls_security_group_endpoint_id = module.network.security_group_vpc_tls_id
    vpc_endpoint_dynamodb_prefix_id    = module.network.vpc_endpoints["dynamodb"]["prefix_list_id"]
    environment_variables = {
      "LOG_LEVEL"                          = var.app_log_level
      "SNS_TOPIC_ARN"                      = module.sns.sns_topic_arn
      "SNS_TOPIC_NOTIFICATION_ENVIRONMENT" = var.env_short
    }
  }

  metadata_lambda = {
    name                           = format("%s-metadata", local.project)
    filename                       = "${path.module}/../../hello-java/build/libs/hello-java-1.0-SNAPSHOT.jar"
    assets_bucket_arn              = module.storage.assets_bucket_arn
    table_client_registrations_arn = local.table_client_registrations_arn
    environment_variables = {
      "ORGANIZATION_URL"                = "https://www.pagopa.it"
      "CONTACT_PERSON_EMAIL_ADDRESS"    = "pagopa@pec.governo.it"
      "ORGANIZATION_DISPLAY_NAME"       = "PagoPA S.p.A."
      "BASE_PATH"                       = "https://${var.r53_dns_zone.name}"
      "ENTITY_ID"                       = "https://${var.r53_dns_zone.name}/pub-op-full"
      "ORGANIZATION_NAME"               = "PagoPA S.p.A."
      "ACS_URL"                         = var.metadata_info.acs_url
      "SLO_URL"                         = var.metadata_info.slo_url
      "CONTACT_PERSON_COMPANY"          = "PagoPA S.p.A."
      "CLIENT_REGISTRATIONS_TABLE_NAME" = "ClientRegistrations"
      "LOG_LEVEL"                       = var.app_log_level
      "SERVICE_METADATA_BUCKET_NAME"    = module.storage.assets_bucket_name
    }
    vpc_id                            = module.network.vpc_id
    vpc_subnet_ids                    = module.network.intra_subnets_ids
    vpc_endpoint_dynamodb_prefix_id   = module.network.vpc_endpoints["dynamodb"]["prefix_list_id"]
    vpc_s3_prefix_id                  = module.network.vpc_endpoints["s3"]["prefix_list_id"]
    vpc_endpoint_ssm_nsg_ids          = tolist(module.network.vpc_endpoints["ssm"].security_group_ids)
    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
  }


  dynamodb_clients_table_stream_arn          = module.database.dynamodb_clients_table_stream_arn
  dynamodb_table_stream_registrations_arn    = module.database.dynamodb_clients_table_stream_arn
  dynamodb_table_stream_arn                  = module.database.dynamodb_table_stream_arn
  table_last_idp_used_arn                    = module.database.table_last_idp_used_arn
  lambda_client_registration_trigger_enabled = false
  eventbridge_pipe_sessions = {
    pipe_name                     = format("%s-sessions-pipe", local.project)
    kms_sessions_table_alias      = module.database.kms_sessions_table_alias_arn
    maximum_retry_attempts        = var.dlq_assertion_setting.maximum_retry_attempts
    maximum_record_age_in_seconds = var.dlq_assertion_setting.maximum_record_age_in_seconds
  }

  assertion_lambda = {
    name     = format("%s-assertion", local.project)
    filename = "${path.module}/../../hello-python/lambda.zip"
    # ⚠️ warning: before switching this values you need to create the resources in the account which is intended
    # to preserve the assertions
    s3_assertion_bucket_arn = "arn:aws:s3:::assertions-3444"
    kms_assertion_key_arn   = "arn:aws:kms:eu-central-1:980921732883:key/3c6f6ba6-90e9-46df-872b-d5aa081940cf"
    #s3_assertion_bucket_arn = module.storage.assertions_bucket_arn
    #kms_assertion_key_arn   = module.storage.kms_assertion_key_arn

    environment_variables = {
      S3_BUCKET = "assertions-3444"
    }
    vpc_id                            = module.network.vpc_id
    vpc_subnet_ids                    = module.network.intra_subnets_ids
    vpc_s3_prefix_id                  = module.network.vpc_endpoints["s3"]["prefix_list_id"]
    vpc_tls_security_group_id         = module.network.security_group_vpc_tls_id
    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
  }

  idp_metadata_lambda = {
    name     = format("%s-update-idp-metadata", local.project)
    filename = "${path.module}/../../hello-java/build/libs/hello-java-1.0-SNAPSHOT.jar"
    environment_variables = {
      IDP_METADATA_BUCKET_NAME = module.storage.s3_idp_metadata_bucket_name
      IDP_TABLE_NAME           = module.database.table_idp_metadata_name
      IDP_G_IDX                = module.database.table_idp_metadata_idx_name
      LOG_LEVEL                = var.app_log_level
    }
    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
    s3_idp_metadata_bucket_arn        = module.storage.idp_metadata_bucket_arn
    s3_idp_metadata_bucket_id         = module.storage.s3_idp_metadata_bucket_name
    vpc_id                            = module.network.vpc_id
    vpc_subnet_ids                    = module.network.intra_subnets_ids
    vpc_s3_prefix_id                  = module.network.vpc_endpoints["s3"]["prefix_list_id"]
  }

  dynamodb_table_idpMetadata = {
    gsi_pointer_arn = local.table_idpMetadata_gsi_pointer_arn
    table_arn       = local.table_idp_metadata_arn
  }

  dynamodb_table_idpStatus = {
    gsi_pointer_arn = module.database.table_idp_status_gsi_pointer_arn
    table_arn       = module.database.table_idp_status_history_arn
  }

  dynamodb_table_clientStatus = {
    gsi_pointer_arn = module.database.table_client_status_gsi_pointer_arn
    table_arn       = module.database.table_client_status_history_arn
  }

  is_gh_integration_lambda = {
    name                              = format("%s-is-gh-integration-lambda", local.project)
    filename                          = "${path.module}/../../hello-java/build/libs/hello-java-1.0-SNAPSHOT.jar"
    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
    sns_topic_arn                     = var.is_gh_sns_arn
    environment_variables             = { LOG_LEVEL = var.app_log_level }
  }

  update_status_lambda = {
    name                              = format("%s-update-status", local.project)
    filename                          = "${path.module}/../../hello-python/lambda.zip"
    assets_bucket_arn                 = module.storage.assets_bucket_arn
    vpc_id                            = module.network.vpc_id
    vpc_subnet_ids                    = module.network.intra_subnets_ids
    vpc_s3_prefix_id                  = module.network.vpc_endpoints["s3"]["prefix_list_id"]
    vpc_endpoint_dynamodb_prefix_id   = module.network.vpc_endpoints["dynamodb"]["prefix_list_id"]
    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
    environment_variables = {
      LOG_LEVEL                    = var.app_log_level
      IDP_STATUS_DYNAMODB_TABLE    = module.database.table_idp_status_history_name
      IDP_STATUS_DYNAMODB_IDX      = module.database.table_idp_status_history_idx_name
      CLIENT_STATUS_DYNAMODB_TABLE = module.database.table_client_status_history_name
      CLIENT_STATUS_DYNAMODB_IDX   = module.database.table_client_status_history_idx_name
      ASSETS_S3_BUCKET             = module.storage.assets_bucket_name
      IDP_STATUS_S3_FILE_NAME      = "idp_status_history.json"
      CLIENT_STATUS_S3_FILE_NAME   = "client_status_history.json"
    }
  }

  retrieve_status_lambda = {
    name                              = format("%s-retrieve-status", local.project)
    filename                          = "${path.module}/../../hello-python/lambda.zip"
    assets_bucket_arn                 = module.storage.assets_bucket_arn
    vpc_id                            = module.network.vpc_id
    vpc_subnet_ids                    = module.network.intra_subnets_ids
    vpc_endpoint_dynamodb_prefix_id   = module.network.vpc_endpoints["dynamodb"]["prefix_list_id"]
    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
    environment_variables = {
      LOG_LEVEL                                 = var.app_log_level
      DYNAMODB_IDP_STATUS_HISTORY_TABLE_NAME    = module.database.table_idp_status_history_name
      DYNAMODB_CLIENT_STATUS_HISTORY_TABLE_NAME = module.database.table_client_status_history_name
    }
  }
  rest_api_id = module.frontend.rest_api_id

  aws_caller_identity   = data.aws_caller_identity.current.account_id
  switch_region_enabled = true

  client_alarm = {
    clients   = local.clients
    namespace = "${local.project}-core/ApplicationMetrics"
  }
  idp_alarm = {
    entity_id = local.idp_entity_ids
    namespace = "${local.project}-core/ApplicationMetrics"
  }

}


module "frontend" {
  source = "../../modules/frontend"

  ## DNS
  domain_name     = "oneid.pagopa.it"
  r53_dns_zone_id = "Z065844519UG4CA4QH19U"

  create_dns_record = false
  role_prefix       = local.project

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

  client_registration_lambda_arn = module.backend.client_registration_lambda_arn
  retrieve_status_lambda_arn     = module.backend.retrieve_status_lambda_arn
  aws_region                     = var.aws_region
  assets_bucket_arn              = module.storage.assets_bucket_arn
  assets_bucket_name             = module.storage.assets_bucket_name
  api_cache_cluster_enabled      = var.api_cache_cluster_enabled
  api_method_settings            = var.api_method_settings

  assets_control_panel_bucket_name = module.storage.assets_control_panel_bucket_name
  assets_control_panel_bucket_arn  = module.storage.assets_control_panel_bucket_arn

  xray_tracing_enabled = var.xray_tracing_enabled
  api_alarms           = local.cloudwatch__api_alarms_with_sns

  web_acl = {
    name                       = format("%s-webacl", local.project)
    cloudwatch_metrics_enabled = true
    sampled_requests_enabled   = true
    sns_topic_arn              = module.sns.sns_topic_arn
  }
  domain_admin_name           = "admin"
  api_gateway_admin_plan      = null
  rest_api_admin_name         = null
  openapi_admin_template_file = null
  client_manager_lambda_arn   = null

}


## Monitoring / Dashboard ##

module "monitoring" {
  env_short                       = var.env_short
  region_short                    = var.aws_region_short
  source                          = "../../modules/monitoring"
  main_dashboard_name             = format("%s-overall-dashboard", local.project)
  api_methods_dashboard_name      = format("%s-api-methods-dashboard", local.project)
  detailed_metrics_dashboard_name = format("%s-detailed-metrics-dashboard", local.project)
  idp_entity_ids                  = local.idp_entity_ids
  clients                         = local.clients
  aws_region                      = var.aws_region
  api_name                        = module.frontend.api_name
  sessions_table                  = module.database.table_sessions_name
  client_registrations_table      = "ClientRegistrations"
  nlb = {
    target_group_arn_suffix = module.backend.nlb_target_group_suffix_arn
    arn_suffix              = module.backend.nlb_arn_suffix
  }
  ecs = {
    service_name   = module.backend.ecs_service_name,
    cluster_name   = module.backend.ecs_cluster_name,
    log_group_name = module.backend.ecs_core_log_group_name
  }

  lambda_client_registration = {
    log_group_name = module.backend.client_registration_log_group_name
  }

  lambda_metadata = {
    log_group_name = module.backend.metadata_lambda_log_group_name
  }

  alarm_subscribers = var.alarm_subscribers
}
