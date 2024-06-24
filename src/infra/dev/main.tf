data "aws_caller_identity" "current" {}

module "iam" {
  source = "../modules/iam"

  github_repository = "pagopa/oneidentity"
}

module "network" {
  source   = "../modules/network"
  vpc_name = format("%s-vpc", local.project)

  azs = ["eu-south-1a", "eu-south-1b", "eu-south-1c"]

  vpc_cidr                  = "10.0.0.0/17"
  vpc_private_subnets_cidr  = ["10.0.80.0/20", "10.0.64.0/20", "10.0.48.0/20"]
  vpc_public_subnets_cidr   = ["10.0.120.0/21", "10.0.112.0/21", "10.0.104.0/21"]
  vpc_internal_subnets_cidr = ["10.0.32.0/20", "10.0.16.0/20", "10.0.0.0/20"]
  enable_nat_gateway        = false
  single_nat_gateway        = true

}

module "frontend" {
  source            = "../modules/frontend"
  vpc_id            = module.network.vpc_id
  public_subnet_ids = module.network.public_subnet_ids

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

metadata_lamba_name = module.backend.metadata_lambda_name

}



module "storage" {
  source = "../modules/storage"

  assertion_bucket = {
    name_prefix              = "assertions"
    gracier_transaction_days = 90
    expiration_days          = 100
    enable_key_rotation      = true
  }

}


module "backend" {
  source = "../modules/backend"

  ecr_registers = [
    {
      name                            = local.ecr_oneid_core
      number_of_images_to_keep        = var.number_of_images_to_keep
      repository_image_tag_mutability = var.repository_image_tag_mutability

  }]

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
      name          = "oneid-core"
      cpu           = var.ecs_oneid_core.container_cpu
      memory        = var.ecs_oneid_core.container_memory
      image_name    = local.ecr_oneid_core
      image_version = var.ecs_oneid_core.image_version
      containerPort = 8080
      hostPort      = 8080
    }

    autoscaling = var.ecs_oneid_core.autoscaling

    subnet_ids = module.network.private_subnet_ids

  }

  ## NLB ##
  nlb_name = format("%s-nlb", local.project)

  github_repository = "pagopa/oneidentity"
  account_id        = data.aws_caller_identity.current.account_id

  table_sessions_arn             = module.database.table_sessions_arn
  table_client_registrations_arn = module.database.table_client_registrations_arn

  kms_sessions_table_alias_arn = module.database.kms_sessions_table_alias_arn

  client_registration_lambda = {
    name                           = format("%s-client-registration", local.project)
    filename                       = "${path.module}/../../hello-java/build/libs/hello-java-1.0-SNAPSHOT.jar"
    table_client_registrations_arn = module.database.table_client_registrations_arn

  }
 
 metadata_lambda = {
    name                           = format("%s-metadata", local.project)
    filename                       = "${path.module}/../../hello-java/build/libs/hello-java-1.0-SNAPSHOT.jar"
    table_client_registrations_arn = module.database.table_client_registrations_arn

  }

}

module "database" {
  source = "../modules/database"

  sessions_table = {
    point_in_time_recovery_enabled = false
  }

  client_registrations_table = {
    point_in_time_recovery_enabled = true
  }

}