## Import ##

import {
  to = module.storage.aws_athena_database.assertions
  id = "assertions"
}


## Moved ##

moved {
  from = module.frontend.module.zones.aws_route53_zone.this["uat.oneid.pagopa.it"]
  to   = module.r53_zones.module.r53_zones.aws_route53_zone.this["uat.oneid.pagopa.it"]
}
