## Import ##

## Move ##

moved {
  from = module.r53_zones.aws_route53_zone.this["oneid.pagopa.it"]
  to   = module.r53_zones.module.r53_zones.aws_route53_zone.this["oneid.pagopa.it"]
}

moved {
  from = module.storage.module.s3_athena_output_bucket
  to   = module.storage.module.s3_athena_output_bucket[0]
}
