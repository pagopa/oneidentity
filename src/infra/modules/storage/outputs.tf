output "assertions_bucket_arn" {
  value = module.s3_assetions_bucket.s3_bucket_arn
}

output "assertions_bucket_name" {
  value = module.s3_assetions_bucket.s3_bucket_id
}

output "metadata_bucket_arn" {
  value = module.s3_metadata_bucket.s3_bucket_arn
}

output "metadata_bucket_name" {
  value = module.s3_metadata_bucket.s3_bucket_id
}