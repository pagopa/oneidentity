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
