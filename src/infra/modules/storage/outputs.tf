output "assertions_bucket_arn" {
  value = module.s3_assertions_bucket.s3_bucket_arn
}

output "assertions_bucket_name" {
  value = module.s3_assertions_bucket.s3_bucket_id
}
