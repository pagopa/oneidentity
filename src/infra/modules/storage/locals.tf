locals {
  bucket_name = format("%s-%s", var.assertion_bucket.name_prefix,
    random_integer.assertion_bucket_suffix.result
  )
  athena_outputs      = format("query-%s", local.bucket_name)
  assets_bucket       = format("%s-%s", var.assets_bucket_prefix, random_integer.asset_bucket_suffix.result)
  idp_metadata_bucket = format("%s-%s", var.idp_metadata_bucket_prefix, random_integer.idp_metadata_bucket_suffix.result)
}
