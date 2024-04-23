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

  r53_dns_zones = {
    "${var.r53_dns_zone.name}" = {
      comment = var.r53_dns_zone.comment
    }

    "oneidentity.pagopa.it" = {
      comment = "Temporary production zone"
    }
  }

  r53_dns_zone_records = [
    {
      name = ""
      type = "A"
      alias = {
        name                   = module.alb.dns_name
        zone_id                = module.alb.zone_id
        evaluate_target_health = true
      }
    },
  ]
}


/* Temporary record that need to be replaces one the production account will be set */
module "records_prod" {
  source  = "terraform-aws-modules/route53/aws//modules/records"
  version = "2.11.0"

  zone_name = "oneidentity.pagopa.it"

  records = [
    {
      name = split(".", var.r53_dns_zone.name)[0]
      type = "NS"
      ttl  = var.dns_record_ttl
      records = [
        "ns-122.awsdns-15.com",
        "ns-1374.awsdns-43.org",
        "ns-1590.awsdns-06.co.uk",
        "ns-649.awsdns-17.net",
      ]
    },
  ]

  depends_on = [module.network]
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
      name                     = local.ecr_idp
      number_of_images_to_keep = 3
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
      target_group_arn  = module.alb.target_groups["ecs_oneidentity"].arn
      security_group_id = module.alb.security_group_id
    }

  }

}


module "database" {
  source = "../modules/database"

  saml_responses_table = {
    name                           = format("%s-saml-responses", local.project)
    point_in_time_recovery_enabled = var.table_saml_responses_point_in_time_recovery_enabled
  }

}