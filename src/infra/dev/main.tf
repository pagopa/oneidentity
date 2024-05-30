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
  source   = "../modules/frontend"
  alb_name = local.alb_name

  vpc_id              = module.network.vpc_id
  public_subnet_ids   = module.network.public_subnet_ids
  acm_certificate_arn = module.frontend.acm_certificate_arn

  r53_dns_zones = {
    "${var.r53_dns_zone.name}" = {
      comment = var.r53_dns_zone.comment
    }
  }
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
      name                            = local.ecr_idp
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

  service_idp = {
    service_name = format("%s-ipd", local.project)

    cpu    = 1024
    memory = 2048

    container = {
      name          = "idp"
      cpu           = 1024
      memory        = 2048
      image_name    = local.ecr_idp
      image_version = var.idp_image_version
      containerPort = 8080
      hostPort      = 8080
    }

    autoscaling = var.ecs_autoscaling_idp

    subnet_ids = module.network.private_subnet_ids

    load_balancer = {
      target_group_arn  = module.frontend.alb_target_groups["ecs_oneidentity"].arn
      security_group_id = module.frontend.alb_security_group_id
    }

  }

  github_repository = "pagopa/oneidentity"
  account_id        = data.aws_caller_identity.current.account_id

  table_saml_responces_arn = module.database.table_saml_responses_arn

  client_registration_lambda = {
    name                           = format("%s-client-registration", local.project)
    filename                       = "${path.module}/../../hello-java/build/libs/hello-java-1.0-SNAPSHOT.jar"
    table_client_registrations_arn = module.database.table_client_registrations_arn

  }

}

module "database" {
  source = "../modules/database"

  saml_responses_table = {
    name                           = "SamlResponses"
    point_in_time_recovery_enabled = false
  }

  client_registrations_table = {
    name                           = "ClientRegistrations"
    point_in_time_recovery_enabled = true
  }

}