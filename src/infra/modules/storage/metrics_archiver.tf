resource "random_integer" "metrics_archiver_bucket_suffix" {
  min = 1000
  max = 9999
}

module "s3_metrics_archiver_bucket" {
  count   = var.metrics_archiver_enabled ? 1 : 0
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.metrics_archiver_bucket
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  tags = {
    Name = local.metrics_archiver_bucket
  }
}