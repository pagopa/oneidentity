locals {
  bucket_name = format("%s-%s", var.assertion_bucket.name_prefix,
    random_integer.assertion_bucket_suffix.result
  )
  athena_outputs = format("query-%s", local.bucket_name)
  assets_bucket = format("%s-%s",
    var.assets_bucket_prefix,
  random_integer.asset_bucket_suffix.result)
  idp_metadata_bucket = format("%s-%s", var.idp_metadata_bucket_prefix, random_integer.idp_metadata_bucket_suffix.result)
  lambda_code_bucket  = format("%s-%s", var.lambda_code_bucket_prefix, random_integer.idp_metadata_bucket_suffix.result)

  replication_configuration = [
    {
      role = try(aws_iam_role.replication[0].arn, null)

      rules = [
        {
          id     = try(var.assertion_bucket.replication_configuration.id, null)
          status = "Enabled"

          delete_marker_replication = false

          source_selection_criteria = {
            replica_modifications = {
              status = "Enabled"
            }
            sse_kms_encrypted_objects = {
              enabled = true
            }
          }

          destination = {
            bucket             = try(var.assertion_bucket.replication_configuration.destination_bucket_arn, null)
            storage_class      = "STANDARD"
            replica_kms_key_id = try(var.assertion_bucket.replication_configuration.kms_key_replica_arn, null)
            account_id         = var.account_id
          }

          filter = {
            prefix = "" # Replicate all objects
          }
        }
      ]
    }, {}
    ][
    var.assertion_bucket.replication_configuration != null ? 0 : 1
  ]

}
