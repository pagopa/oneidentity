data "aws_caller_identity" "current" {}

module "iam" {
  source = "../modules/iam"

  github_repository = "pagopa/oneidentity"
}

module "network" {
  source   = "../modules/network"
  vpc_name = format("%s-vpc", local.project)

  azs = ["eu-south-1a", "eu-south-1b", "eu-south-1c"]

  vpc_cidr                  = var.vpc_cidr
  vpc_private_subnets_cidr  = var.vpc_private_subnets_cidr
  vpc_public_subnets_cidr   = var.vpc_public_subnets_cidr
  vpc_internal_subnets_cidr = var.vpc_internal_subnets_cidr
  enable_nat_gateway        = var.enable_nat_gateway
  single_nat_gateway        = var.single_nat_gateway

}

module "frontend" {
  source = "../modules/frontend"

  ## API Gateway ##
  rest_api_name = format("%s-restapi", local.project)

  r53_dns_zones = {
    "${var.r53_dns_zone.name}" = {
      comment = var.r53_dns_zone.comment
    }
  }

  api_gateway_target_arns = [module.backend.nlb_arn]
  nlb_dns_name            = module.backend.nlb_dns_name

  api_gateway_plan = {
    name                 = format("%s-restapi-plan", local.project)
    throttle_burst_limit = var.rest_api_throttle_settings.burst_limit
    throttle_rate_limit  = var.rest_api_throttle_settings.rate_limit
  }

  # TODO fix
  metadata_lamba_name            = module.backend.metadata_lambda_name
  metadata_lamba_arn             = module.backend.metadata_lambda_arn
  client_registration_lambda_arn = module.backend.client_registration_lambda_arn
  aws_region                     = var.aws_region
  api_cache_cluster_enabled      = var.api_cache_cluster_enabled
  api_method_settings            = var.api_method_settings

}

module "storage" {
  source = "../modules/storage"

  assertion_bucket = {
    name_prefix              = "assertions"
    glacier_transaction_days = 90
    expiration_days          = 100
    enable_key_rotation      = true
  }

}

module "backend" {
  source = "../modules/backend"

  aws_region = var.aws_region

  ecr_registers = [
    {
      name                            = local.ecr_oneid_core
      number_of_images_to_keep        = var.number_of_images_to_keep
      repository_image_tag_mutability = var.repository_image_tag_mutability
    }
  ]

  ecs_cluster_name          = format("%s-ecs", local.project)
  enable_container_insights = true

  fargate_capacity_providers = {
    FARGATE = {
      default_capacity_provider_strategy = {
        weight = 50
        base   = 20
      }
    }
  }

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
    pipe_name                = format("%s-sessions-pipe", local.project)
    kms_sessions_table_alias = module.database.kms_sessions_table_alias_arn
  }

  assertion_lambda = {
    name                    = format("%s-assertion", local.project)
    source_path             = "${path.module}/../../oneid/oneid-lambda-assertion"
    s3_assertion_bucket_arn = module.storage.assertions_bucket_arn
    kms_assertion_key_arn   = module.storage.kms_assertion_key_arn

    environment_variables = {
      S3_BUCKET = module.storage.assertions_bucket_name
    }

    cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_logs_retention_in_days
  }
}

module "spid_validator" {
  source = "../modules/spid-validator"

  aws_region = var.aws_region

  ecr_repository_name = format("%s-spid-validator", local.project)

  spid_validator = {
    cluster_arn  = module.backend.ecs_cluster_arn
    service_name = format("%s-spid-validator", local.project)
    container = {
      name          = "validator"
      image_name    = format("%s-spid-validator", local.project)
      image_version = "2.0.0"
      environment = [
        {
          name  = "NODE_USE_HTTPS"
          value = "false"
          }, {
          name  = "NODE_HTTPS_PORT"
          value = "8080"
        },
        {
          name  = "SPID_USERS_URL"
          value = "https://raw.githubusercontent.com/pagopa/oneidentity/main/src/config/validator/users.json"
        }
      ]
    }
  }

  vpc_id              = module.network.vpc_id
  public_subnet_ids   = module.network.public_subnet_ids
  private_subnets_ids = module.network.private_subnet_ids

  alb_spid_validator_name = format("%s-spid-validator-alb", local.project)
  vpc_cidr_block          = module.network.vpc_cidr_block

  zone_id   = module.frontend.route53_zone_id
  zone_name = module.frontend.zone_name

}

module "database" {
  source         = "../modules/database"
  sessions_table = var.sessions_table
}


## Monitoring 

module "monitoring" {
  source              = "../modules/monitoring"
  main_dashboard_name = format("%s-overall-dashboard", local.project)
  aws_region          = var.aws_region
  api_name            = module.frontend.api_name
  dynamodb_table_name = module.database.table_sessions_name
  nlb = {
    target_group_arn_suffix = module.backend.nlb_target_group_suffix_arn
    arn_suffix              = module.backend.nlb_arn_suffix
  }
  ecs = {
    service_name = module.backend.ecs_service_name,
    cluster_name = module.backend.ecs_cluster_name
  }
}
