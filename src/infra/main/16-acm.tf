module "acm" {
  source  = "terraform-aws-modules/acm/aws"
  version = "5.0.0"

  domain_name = var.r53_dns_zone.name

  zone_id = module.zones.route53_zone_zone_id[var.r53_dns_zone.name]

  validation_method      = "DNS"
  create_route53_records = true

  tags = {
    Name = format("%s-acm", local.project)
  }
}