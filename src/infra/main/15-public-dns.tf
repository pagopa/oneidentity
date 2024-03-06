module "zones" {
  source  = "terraform-aws-modules/route53/aws//modules/zones"
  version = "2.11.0"

  zones = {


    "${var.r53_dns_zone.name}" = {
      comment = var.r53_dns_zone.comment
    }
  }

  tags = {
    Name = var.r53_dns_zone.name
  }
}

