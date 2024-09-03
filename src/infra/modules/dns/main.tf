## DNS Zones
module "r53_zones" {
  source  = "terraform-aws-modules/route53/aws//modules/zones"
  version = "2.11.0"
  zones   = var.r53_dns_zones
}
