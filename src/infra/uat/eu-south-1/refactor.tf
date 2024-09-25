## Import ##


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
