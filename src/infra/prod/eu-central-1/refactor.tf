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