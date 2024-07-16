output "assertions_bucket_arn" {
  value = module.s3_assertions_bucket.s3_bucket_arn
}

output "assertions_bucket_name" {
  value = module.s3_assertions_bucket.s3_bucket_id
}

output "kms_assertion_key_arn" {
  value = module.kms_assertions_bucket.aliases["assertions/S3"].target_key_arn
}
