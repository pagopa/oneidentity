## Import ##

import {
  to = module.backend.aws_ssm_parameter.key_pem
  id = "key.pem"
}

import {
  to = module.storage.aws_athena_database.assertions[0]
  id = "assertions"
}

import {
  to = module.database.module.dynamodb_table_client_registrations[0].aws_dynamodb_table.this[0]
  id = "ClientRegistrations"
}

import {
  to = module.backend.aws_pipes_pipe.sessions
  id = "oneid-io-es-1-p-sessions-pipe"
}

# ## Move ##

# moved {
#   from = module.r53_zones.aws_route53_zone.this["io.oneid.pagopa.it"]
#   to   = module.r53_zones.module.r53_zones.aws_route53_zone.this["io.oneid.pagopa.it"]
# }

# moved {
#   from = module.storage.module.s3_athena_output_bucket
#   to   = module.storage.module.s3_athena_output_bucket[0]
# }

# moved {
#   from = module.database.module.dynamodb_table_idpMetadata
#   to   = module.database.module.dynamodb_table_idpMetadata[0]
# }

moved {
  from = module.database.module.dynamodb_table_client_registrations
  to   = module.database.module.dynamodb_table_client_registrations[0]
}

moved {
  from = module.backend.aws_pipes_pipe.sessions[0]
  to   = module.backend.aws_pipes_pipe.sessions
}

# moved {
#   from = module.frontend.module.records.aws_route53_record.this[" A"]
#   to   = module.frontend.module.records[0].aws_route53_record.this[" A"]
# }

# moved {
#   from = module.storage.module.s3_assets_bucket
#   to   = module.storage.module.s3_assets_bucket[0]
# }

# moved {
#   from = module.storage.aws_iam_policy.github_s3_policy
#   to   = module.storage.aws_iam_policy.github_s3_policy[0]
# }

# moved {
#   from = module.storage.module.s3_idp_metadata_bucket
#   to   = module.storage.module.s3_idp_metadata_bucket[0]
# }