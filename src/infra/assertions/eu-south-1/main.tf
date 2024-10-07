data "aws_caller_identity" "current" {}

module "storage" {
  source = "../../modules/storage"

  role_prefix = local.project

  assertion_bucket = {
    name_prefix               = "assertions"
    glacier_transaction_days  = var.assertion_bucket.glacier_transaction_days
    expiration_days           = var.assertion_bucket.expiration_days
    enable_key_rotation       = var.assertion_bucket.enable_key_rotation
    kms_multi_region          = var.assertion_bucket.kms_multi_region
    object_lock_configuration = var.assertion_bucket.object_lock_configuration

    replication_configuration = var.assertion_bucket.replication_configuration
  }
  assertions_crawler_schedule = var.assertions_crawler_schedule
  idp_metadata_bucket_prefix  = "idp-metadata"
  assets_bucket_prefix        = "assets"
  github_repository           = "pagopa/oneidentity"
  account_id                  = data.aws_caller_identity.current.account_id
}

