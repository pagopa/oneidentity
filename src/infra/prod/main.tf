data "aws_caller_identity" "current" {}


## DNS Zones
/* This is temporary since the definitio already exists in the frontend module. */
module "r53_zones" {
  source  = "terraform-aws-modules/route53/aws//modules/zones"
  version = "2.11.0"

  zones = {
    "${var.r53_dns_zone.name}" = {
      comment = var.r53_dns_zone.comment
    }
  }
}

/*
module "iam" {
  source = "../modules/iam"

  github_repository = "pagopa/oneidentity"
}

*/
