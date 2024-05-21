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

module "dev_ns_record" {
  source  = "terraform-aws-modules/route53/aws//modules/records"
  version = "2.11.0"

  zone_name = var.r53_dns_zone.name

  records = [
    {
      name = split(".", var.r53_dns_zone.name)[0]
      type = "NS"
      ttl  = var.dns_record_ttl
      records = [
        "ns-1299.awsdns-34.org",
        "ns-174.awsdns-21.com",
        "ns-1856.awsdns-40.co.uk",
        "ns-936.awsdns-53.net",
      ]
    },
  ]

  depends_on = [module.r53_zones]

}

/*
module "iam" {
  source = "../modules/iam"

  github_repository = "pagopa/oneidentity"
}

*/
