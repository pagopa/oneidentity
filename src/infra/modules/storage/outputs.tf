output "assertions_bucket_arn" {
  value = module.s3_assertions_bucket.s3_bucket_arn
}

output "assertions_bucket_name" {
  value = module.s3_assertions_bucket.s3_bucket_id
}

output "idp_metadata_bucket_arn" {
  value = module.s3_idp_metadata_bucket.s3_bucket_arn
}

output "s3_idp_metadata_bucket_name" {
  value = module.s3_idp_metadata_bucket.s3_bucket_id
}

output "kms_assertion_key_arn" {
  value = module.kms_assertions_bucket.aliases["assertions/S3"].target_key_arn
}

output "assets_bucket_arn" {
  value = try(module.s3_assets_bucket[0].s3_bucket_arn, null)
}
output "assets_bucket_name" {
  value = try(module.s3_assets_bucket[0].s3_bucket_id, null)
}

output "deploy_assets_role" {
  value = aws_iam_role.githubS3deploy.arn
}
