locals {
  kms_sessions_table_alias = "/dynamodb/sessions"
  gsi_code                 = "gsi_code_idx"
  gsi_pointer              = "gsi_pointer_idx"
}


module "kms_sessions_table" {
  source  = "terraform-aws-modules/kms/aws"
  version = "3.0.0"

  description             = "KMS key for Dynamodb table encryption."
  key_usage               = "ENCRYPT_DECRYPT"
  enable_key_rotation     = var.kms_ssm_enable_rotation
  rotation_period_in_days = var.kms_rotation_period_in_days

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

  server_side_encryption_enabled = true
  server_side_encryption_kms_key_arn = module.kms_sessions_table.aliases[
  local.kms_sessions_table_alias].target_key_arn

  stream_enabled              = var.sessions_table.stream_enabled
  stream_view_type            = var.sessions_table.stream_view_type
  deletion_protection_enabled = var.sessions_table.deletion_protection_enabled

  tags = {
    Name = "Session"
  }

}

module "dynamodb_table_client_registrations" {
  count   = var.client_registrations_table != null ? 1 : 0
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

  point_in_time_recovery_enabled = var.client_registrations_table.point_in_time_recovery_enabled
  stream_enabled                 = var.client_registrations_table.stream_enabled
  stream_view_type               = var.client_registrations_table.stream_view_type
  replica_regions                = var.client_registrations_table.replication_regions
  deletion_protection_enabled    = var.client_registrations_table.deletion_protection_enabled
  tags = {
    Name = "ClientRegistrations"
  }

}

data "aws_dynamodb_table" "dynamodb_table_client_registrations" {
  count = var.client_registrations_table == null ? 1 : 0
  name  = "ClientRegistrations"
}

module "dynamodb_table_idpMetadata" {
  count   = var.idp_metadata_table != null ? 1 : 0
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = "IDPMetadata"

  hash_key  = "entityID"
  range_key = "pointer"

  global_secondary_indexes = [
    {
      name            = local.gsi_pointer
      hash_key        = "pointer"
      projection_type = "ALL"
    }
  ]

  attributes = [
    {
      name = "entityID"
      type = "S"
    },
    {
      name = "pointer"
      type = "S"
    },
  ]

  billing_mode = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.idp_metadata_table.point_in_time_recovery_enabled
  stream_enabled                 = var.idp_metadata_table.stream_enabled
  stream_view_type               = var.idp_metadata_table.stream_view_type
  replica_regions                = var.idp_metadata_table.replication_regions
  deletion_protection_enabled    = var.idp_metadata_table.deletion_protection_enabled
  tags = {
    Name = "IDPMetadata"
  }

}

data "aws_dynamodb_table" "dynamodb_table_idp_status_history" {
  count = var.idp_status_history_table == null ? 1 : 0
  name  = "IDPStatusHistory"
}

module "dynamodb_table_idp_status_history" {
  count   = var.idp_status_history_table != null ? 1 : 0
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = "IDPStatusHistory"

  hash_key  = "entityID"
  range_key = "pointer"

  global_secondary_indexes = [
    {
      name            = local.gsi_pointer
      hash_key        = "pointer"
      projection_type = "ALL"
    }
  ]

  attributes = [
    {
      name = "entityID"
      type = "S"
    },
    {
      name = "pointer"
      type = "S"
    },
  ]

  billing_mode = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.idp_status_history_table.point_in_time_recovery_enabled
  stream_enabled                 = var.idp_status_history_table.stream_enabled
  stream_view_type               = var.idp_status_history_table.stream_view_type
  replica_regions                = var.idp_status_history_table.replication_regions
  deletion_protection_enabled    = var.idp_status_history_table.deletion_protection_enabled
  tags = {
    Name = "IDPStatusHistory"
  }

}

resource "aws_dynamodb_table_item" "default_idp_status_history_item" {
  table_name = module.dynamodb_table_idp_status_history[0].dynamodb_table_id
  hash_key   = "entityID"
  range_key  = "pointer"
  lifecycle {
    ignore_changes = [
      item
    ]
  }
  for_each = var.idp_entity_ids != null ? { for s in var.idp_entity_ids.entity_id : s => s } : {}
  item     = <<ITEM
  {
    "entityID": {"S": "${each.key}"},
    "pointer": {"S": "latest"},
    "idpStatus": {"S": "OK"}
  }
  ITEM
}