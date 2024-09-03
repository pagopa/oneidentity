## Move ##

moved {
  from = module.r53_zones.aws_route53_zone.this["oneid.pagopa.it"]
  to   = module.r53_zones.module.r53_zones.aws_route53_zone.this["oneid.pagopa.it"]
}
