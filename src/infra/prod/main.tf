data "aws_caller_identity" "current" {}


## DNS Zones
/* This is temporary since the definitio already exists in the frontend module. */
# https://excalidraw.com/#json=7JsEzD4q1OklkLKu-nqmw,V6xmyHNRi-6OU-C32Nr_KA
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

  depends_on = [module.r53_zones]

}

/*
module "iam" {
  source = "../modules/iam"

  github_repository = "pagopa/oneidentity"
}

*/
