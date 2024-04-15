module "zones" {
  source  = "terraform-aws-modules/route53/aws//modules/zones"
  version = "2.11.0"

  zones = {
    "${var.r53_dns_zone.name}" = {
      comment = var.r53_dns_zone.comment
    }

    "oneidentity.pagopa.it" = {
      comment = "Temporary production zone"
    }
  }

  tags = {
    Name = var.r53_dns_zone.name
  }
}

module "records" {
  source  = "terraform-aws-modules/route53/aws//modules/records"
  version = "2.11.0"

  zone_name = keys(module.zones.route53_zone_zone_id)[0]

  records = [
    {
      name = var.r53_dns_zone.name
      type = "NS"
      ttl  = var.dns_record_ttl
      records = [
        "ns-122.awsdns-15.com",
        "ns-1374.awsdns-43.org",
        "ns-1590.awsdns-06.co.uk",
        "ns-649.awsdns-17.net",
      ]
    },
    {
      name = var.r53_dns_zone.name
      type = "A"
      alias = {
        name                   = module.alb.dns_name
        zone_id                = module.alb.zone_id
        evaluate_target_health = true
      }
    },
  ]

  depends_on = [module.zones]
}
