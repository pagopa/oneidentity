## Import ##
import {
  to = module.backend.aws_ssm_parameter.key_pem
  id = "key.pem"
}

import {
  to = module.backend.module.security_group_lambda_metadata.aws_security_group_rule.egress_rules[0]
  id = "sg-0eeaa33edc426d187_egress_tcp_443_443_pl-ccb451a5"
}

import {
  to = module.backend.aws_security_group_rule.metadata_s3_tls[0]
  id = "sg-0eeaa33edc426d187_egress_tcp_443_443_pl-daaa4fb3"
}

## Moved ##

moved {
  from = module.frontend.module.zones.aws_route53_zone.this["uat.oneid.pagopa.it"]
  to   = module.r53_zones.module.r53_zones.aws_route53_zone.this["uat.oneid.pagopa.it"]
}

moved {
  from = module.storage.module.s3_athena_output_bucket.aws_s3_bucket.this
  to   = module.storage.module.s3_athena_output_bucket.aws_s3_bucket.this[0]
}

moved {
  from = module.storage.module.s3_athena_output_bucket.aws_s3_bucket_acl.this
  to   = module.storage.module.s3_athena_output_bucket.aws_s3_bucket_acl.this[0]
}

moved {
  from = module.storage.module.s3_athena_output_bucket.aws_s3_bucket_ownership_controls.this
  to   = module.storage.module.s3_athena_output_bucket.aws_s3_bucket_ownership_controls.this[0]
}

moved {
  from = module.storage.module.s3_athena_output_bucket.aws_s3_bucket_public_access_block.this
  to   = module.storage.module.s3_athena_output_bucket.aws_s3_bucket_public_access_block.this[0]
}

moved {
  from = module.storage.module.s3_athena_output_bucket.aws_s3_bucket_server_side_encryption_configuration.this
  to   = module.storage.module.s3_athena_output_bucket.aws_s3_bucket_server_side_encryption_configuration.this[0]
}

moved {
  from = module.storage.module.s3_athena_output_bucket
  to   = module.storage.module.s3_athena_output_bucket[0]

}

moved {
  from = module.backend.module.is-gh-integration-lambda
  to   = module.backend.module.is_gh_integration_lambda
}

moved {
  from = module.database.module.dynamodb_table_idpMetadata
  to   = module.database.module.dynamodb_table_idpMetadata[0]
}

moved {
  from = module.database.module.dynamodb_table_client_registrations.aws_dynamodb_table.this[0]
  to   = module.database.module.dynamodb_table_client_registrations[0].aws_dynamodb_table.this[0]
}

moved {
  from = module.frontend.module.records.aws_route53_record.this[" A"]
  to   = module.frontend.module.records[0].aws_route53_record.this[" A"]
}

moved {
  from = module.storage.module.s3_assets_bucket
  to   = module.storage.module.s3_assets_bucket[0]
}

moved {
  from = module.storage.aws_iam_policy.github_s3_policy
  to   = module.storage.aws_iam_policy.github_s3_policy[0]
}

moved {
  from = module.storage.module.s3_idp_metadata_bucket
  to   = module.storage.module.s3_idp_metadata_bucket[0]
}