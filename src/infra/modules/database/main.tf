locals {
  kms_sessions_table_alias = "/dynamodb/sessions"
  gsi_code                 = "gsi_code_idx"
  gsi_pointer              = "gsi_pointer_idx"
  gsi_namespace            = "gsi_namespace_idx"
}


# Sessions
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
    local.kms_sessions_table_alias
  ].target_key_arn

  stream_enabled              = var.sessions_table.stream_enabled
  stream_view_type            = var.sessions_table.stream_view_type
  deletion_protection_enabled = var.sessions_table.deletion_protection_enabled

  tags = {
    Name = "Session"
  }

}


# Client Registrations
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


# IDP Metadata
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


# Idp Status History
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
  for_each = var.idp_entity_ids != null ? { for i in var.idp_entity_ids : i => i } : {}
  item     = <<ITEM
  {
    "entityID": {"S": "${each.key}"},
    "pointer": {"S": "latest"},
    "idpStatus": {"S": "OK"}
  }
  ITEM
}


## Client Status History
data "aws_dynamodb_table" "dynamodb_table_client_status_history" {
  count = var.client_status_history_table == null ? 1 : 0
  name  = "ClientStatusHistory"
}

module "dynamodb_table_client_status_history" {
  count   = var.client_status_history_table != null ? 1 : 0
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = "ClientStatusHistory"

  hash_key  = "clientID"
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
      name = "clientID"
      type = "S"
    },
    {
      name = "pointer"
      type = "S"
    },
  ]

  billing_mode = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.client_status_history_table.point_in_time_recovery_enabled
  stream_enabled                 = var.client_status_history_table.stream_enabled
  stream_view_type               = var.client_status_history_table.stream_view_type
  replica_regions                = var.client_status_history_table.replication_regions
  deletion_protection_enabled    = var.client_status_history_table.deletion_protection_enabled
  tags = {
    Name = "ClientStatusHistory"
  }

}

resource "aws_dynamodb_table_item" "default_client_status_history_item" {
  table_name = module.dynamodb_table_client_status_history[0].dynamodb_table_id
  hash_key   = "clientID"
  range_key  = "pointer"
  lifecycle {
    ignore_changes = [
      item
    ]
  }
  for_each = var.clients != null ? { for c in var.clients : c.client_id => c } : {}
  item     = <<ITEM
  {
    "clientID": {"S": "${each.key}"},
    "pointer": {"S": "latest"},
    "clientStatus": {"S": "OK"}
  }
  ITEM
}


# LastIDPUsed
data "aws_dynamodb_table" "dynamodb_table_last_idp_used" {
  count = var.last_idp_used_table == null ? 1 : 0
  name  = "LastIDPUsed"
}

module "dynamodb_table_last_idp_used" {
  count   = var.last_idp_used_table != null ? 1 : 0
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = "LastIDPUsed"

  hash_key  = "id"
  range_key = "clientId"

  attributes = [
    {
      name = "id"
      type = "S"
    },
    {
      name = "clientId"
      type = "S"
    }
  ]

  ttl_attribute_name = "ttl"
  ttl_enabled        = var.last_idp_used_table.ttl_enabled
  billing_mode       = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.last_idp_used_table.point_in_time_recovery_enabled
  stream_enabled                 = var.last_idp_used_table.stream_enabled
  stream_view_type               = var.last_idp_used_table.stream_view_type
  replica_regions                = var.last_idp_used_table.replication_regions
  deletion_protection_enabled    = var.last_idp_used_table.deletion_protection_enabled
  tags = {
    Name = "LastIDPUsed"
  }

}

# Internal IDP Mock Users Table

module "dynamodb_table_internal_idp_users" {
  count   = var.internal_idp_users_table != null ? 1 : 0
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = "InternalIDPUsers"

  hash_key  = "username"
  range_key = "namespace"

  attributes = [
    {
      name = "username"
      type = "S"
    },
    {
      name = "namespace"
      type = "S"
    }
  ]

  global_secondary_indexes = [
    {
      name            = local.gsi_namespace
      hash_key        = "namespace"
      projection_type = "ALL"
    }
  ]

  billing_mode = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.internal_idp_users_table.point_in_time_recovery_enabled
  stream_enabled                 = var.internal_idp_users_table.stream_enabled
  stream_view_type               = var.internal_idp_users_table.stream_view_type
  deletion_protection_enabled    = var.internal_idp_users_table.deletion_protection_enabled
  tags = {
    Name = "InternalIDPUsers"
  }

}

# Internal IDP Session Table

module "dynamodb_table_internal_idp_sessions" {
  count   = var.internal_idp_sessions != null ? 1 : 0
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = "InternalIDPSessions"

  hash_key  = "authnRequestId"
  range_key = "clientId"

  attributes = [
    {
      name = "authnRequestId"
      type = "S"
    },
    {
      name = "clientId"
      type = "S"
    }
  ]

  billing_mode = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.internal_idp_sessions.point_in_time_recovery_enabled
  stream_enabled                 = var.internal_idp_sessions.stream_enabled
  stream_view_type               = var.internal_idp_sessions.stream_view_type
  deletion_protection_enabled    = var.internal_idp_sessions.deletion_protection_enabled
  tags = {
    Name = "InternalIDPSessions"
  }

}
