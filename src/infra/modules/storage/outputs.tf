output "assertion_bucket_arn" {
  value = module.s3_assetion_bucket.s3_bucket_arn
}

output "assertion_bucket_name" {
  value = module.s3_assetion_bucket.s3_bucket_id
}