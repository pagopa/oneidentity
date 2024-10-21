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
  create_athena_table         = true
  assertions_crawler_schedule = var.assertions_crawler_schedule
  create_assets_bucket        = false
  create_idp_metadata_bucket  = false
  github_repository           = "pagopa/oneidentity"
  account_id                  = data.aws_caller_identity.current.account_id
}



module "backup" {
  source      = "../../modules/backup"
  backup_name = "s3-backup"
  prefix      = local.project

  backup_rule = [{
    rule_name         = "backup_weekly_rule"
    schedule          = "cron(0 14 * * ? *)"
    start_window      = 60
    completion_window = 140
    lifecycle = {
      delete_after = 14
    },
    },
    {
      rule_name         = "backup_monthly_rule"
      schedule          = "cron(0 14 * * ? *)"
      start_window      = 60
      completion_window = 140
      lifecycle = {
        delete_after = 365
      },
  }]
}
