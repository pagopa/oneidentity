data "aws_caller_identity" "current" {}


module "iam" {
  source            = "../../modules/iam"
  prefix            = local.project
  github_repository = "pagopa/oneidentity"
}

module "r53_zones" {
  source = "../../modules/dns"

  r53_dns_zones = {
    "${var.r53_dns_zone.name}" = {
      comment = var.r53_dns_zone.comment
    }
  }
}

module "dev_ns_record" {
  source  = "terraform-aws-modules/route53/aws//modules/records"
  version = "2.11.0"

  zone_name = module.r53_zones.dns_zone_name

  records = [
    {
      name = "dev"
      type = "NS"
      ttl  = var.dns_record_ttl
      records = [
        "ns-1299.awsdns-34.org",
        "ns-174.awsdns-21.com",
        "ns-1856.awsdns-40.co.uk",
        "ns-936.awsdns-53.net",
      ]
    },
    {
      name = "uat"
      type = "NS"
      ttl  = var.dns_record_ttl
      records = [
        "ns-2033.awsdns-62.co.uk.",
        "ns-1177.awsdns-19.org.",
        "ns-233.awsdns-29.com.",
        "ns-668.awsdns-19.net.",
      ]
    },
  ]
}


module "network" {
  source   = "../../modules/network"
  vpc_name = format("%s-vpc", local.project)

  azs = ["eu-south-1a", "eu-south-1b", "eu-south-1c"]

  vpc_cidr                  = var.vpc_cidr
  vpc_private_subnets_cidr  = var.vpc_private_subnets_cidr
  vpc_public_subnets_cidr   = var.vpc_public_subnets_cidr
  vpc_internal_subnets_cidr = var.vpc_internal_subnets_cidr
  enable_nat_gateway        = var.enable_nat_gateway
  single_nat_gateway        = var.single_nat_gateway

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
  assertions_crawler_schedule = var.assertions_crawler_schedule

  assets_bucket_prefix = "assets"
  github_repository    = "pagopa/oneidentity"
  account_id           = data.aws_caller_identity.current.account_id
}

module "sns" {
  source            = "../../modules/sns"
  sns_topic_name    = format("%s-sns", local.project)
  alarm_subscribers = var.alarm_subscribers
}

