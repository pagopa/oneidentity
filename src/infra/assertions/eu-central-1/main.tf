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

    lambda_role_arn = var.assertion_bucket.lambda_role_arn
  }
  xsw_assertions_bucket = {
    name_prefix               = "xsw-assertions"
    glacier_transaction_days  = var.xsw_assertions_bucket.glacier_transaction_days
    expiration_days           = var.xsw_assertions_bucket.expiration_days
    enable_key_rotation       = var.xsw_assertions_bucket.enable_key_rotation
    kms_multi_region          = var.xsw_assertions_bucket.kms_multi_region
    object_lock_configuration = var.xsw_assertions_bucket.object_lock_configuration
    replication_configuration = var.xsw_assertions_bucket.replication_configuration
  }
  create_athena_table             = false
  assertions_crawler_schedule     = var.assertions_crawler_schedule
  create_assets_bucket            = false
  create_idp_metadata_bucket      = false
  github_repository               = "pagopa/oneidentity"
  account_id                      = data.aws_caller_identity.current.account_id
  assertion_accesslogs_expiration = 180
}

