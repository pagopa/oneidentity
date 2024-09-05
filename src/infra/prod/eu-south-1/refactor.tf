## Import ##

import {
  to = module.storage.aws_athena_database.assertions
  id = "assertions"
}

## Move ##

moved {
  from = module.r53_zones.aws_route53_zone.this["oneid.pagopa.it"]
  to   = module.r53_zones.module.r53_zones.aws_route53_zone.this["oneid.pagopa.it"]
}
