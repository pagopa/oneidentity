module "zones" {
  source  = "terraform-aws-modules/route53/aws//modules/zones"
  version = "2.11.1"

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

resource "aws_route53_record" "dev" {
  allow_overwrite = true
  name            = var.r53_dns_zone.name
  ttl             = var.dns_record_ttl
  type            = "NS"
  zone_id         = module.zones.route53_zone_zone_id["oneidentity.pagopa.it"]

  records = [
    "ns-122.awsdns-15.com",
    "ns-1374.awsdns-43.org",
    "ns-1590.awsdns-06.co.uk",
    "ns-649.awsdns-17.net",
  ]
}

resource "aws_route53_record" "main" {
  zone_id = module.zones.route53_zone_zone_id[var.r53_dns_zone.name]
  name    = var.r53_dns_zone.name
  type    = "A"
  alias {
    name                   = module.alb.dns_name
    zone_id                = module.alb.zone_id
    evaluate_target_health = true
  }
}