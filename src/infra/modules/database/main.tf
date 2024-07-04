locals {
  kms_sessions_table_alias = "/dynamodb/sessions"
  gsi_code                 = "gsi_code_idx"
}


module "kms_sessions_table" {
  source  = "terraform-aws-modules/kms/aws"
  version = "2.2.1"

  description = "KMS key for Dynamodb table encryption."
  key_usage   = "ENCRYPT_DECRYPT"

  # Aliases
  aliases = [local.kms_sessions_table_alias]
}

module "dynamodb_sessions_table" {
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = "Sessions"

  hash_key  = "samlRequestID"
  range_key = "recordType"

  global_secondary_indexes = [
    {
      name            = local.gsi_code
      hash_key        = "code"
      projection_type = "ALL"
    }
  ]

  attributes = [
    {
      name = "samlRequestID"
      type = "S"
    },
    {
      name = "recordType"
      type = "S"
    },
    {
      name = "code"
      type = "S"
    }
  ]

  ttl_attribute_name = "ttl"
  ttl_enabled        = var.sessions_table.ttl_enabled

  billing_mode = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.sessions_table.point_in_time_recovery_enabled

  server_side_encryption_enabled     = true
  server_side_encryption_kms_key_arn = module.kms_sessions_table.aliases[local.kms_sessions_table_alias].target_key_arn

  stream_enabled   = var.sessions_table.stream_enabled
  stream_view_type = var.sessions_table.stream_view_type

  tags = {
    Name = "Session"
  }

}


module "dynamodb_table_client_registrations" {
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = "ClientRegistrations"

  hash_key = "clientId"

  attributes = [
    {
      name = "clientId"
      type = "S"
    }
  ]

  billing_mode = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.sessions_table.point_in_time_recovery_enabled

  tags = {
    Name = "ClientRegistrations"
  }

}
