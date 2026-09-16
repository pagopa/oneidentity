module "s3_metrics_athena_results_bucket" {
  count   = var.metrics_athena_enabled ? 1 : 0
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = format("%s-%s", var.metrics_athena_results_bucket.name_prefix, random_integer.metrics_archiver_bucket_suffix.result)
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  server_side_encryption_configuration = {
    rule = {
      apply_server_side_encryption_by_default = {
        sse_algorithm = "AES256"
      }
    }
  }
}