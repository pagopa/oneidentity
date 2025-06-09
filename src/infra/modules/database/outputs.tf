# Sessions
output "table_sessions_name" {
  value = module.dynamodb_sessions_table.dynamodb_table_id
}

output "table_sessions_arn" {
  value = module.dynamodb_sessions_table.dynamodb_table_arn
}

output "kms_sessions_table_alias_arn" {
  value = module.kms_sessions_table.aliases[local.kms_sessions_table_alias].target_key_arn
}

output "dynamodb_table_stream_arn" {
  value = module.dynamodb_sessions_table.dynamodb_table_stream_arn
}

output "table_sessions_gsi_code_arn" {
  value = "${module.dynamodb_sessions_table.dynamodb_table_arn}/index/${local.gsi_code}"
}

# Client
output "table_client_registrations_name" {
  value = try(module.dynamodb_table_client_registrations[0].dynamodb_table_id,
    data.aws_dynamodb_table.dynamodb_table_client_registrations[0].id
  )
}

output "table_client_registrations_arn" {
  value = try(
    module.dynamodb_table_client_registrations[0].dynamodb_table_arn,
    data.aws_dynamodb_table.dynamodb_table_client_registrations[0].arn
  )
}

# IDP status history
output "table_idp_status_history_name" {
  value = try(module.dynamodb_table_idp_status_history[0].dynamodb_table_id,
    data.aws_dynamodb_table.dynamodb_table_idp_status_history[0].id
  )
}

output "table_idp_status_history_arn" {
  value = try(module.dynamodb_table_idp_status_history[0].dynamodb_table_arn, null)
}

output "table_idp_status_gsi_pointer_arn" {
  value = try("${module.dynamodb_table_idp_status_history[0].dynamodb_table_arn}/index/${local.gsi_pointer}", null)
}

output "table_idp_status_history_idx_name" {
  value = local.gsi_pointer
}

# Client status history
output "table_client_status_history_name" {
  value = try(module.dynamodb_table_client_status_history[0].dynamodb_table_id,
    data.aws_dynamodb_table.dynamodb_table_client_status_history[0].id
  )
}

output "table_client_status_history_arn" {
  value = try(module.dynamodb_table_client_status_history[0].dynamodb_table_arn, null)
}

output "table_client_status_gsi_pointer_arn" {
  value = try("${module.dynamodb_table_client_status_history[0].dynamodb_table_arn}/index/${local.gsi_pointer}", null)
}

output "table_client_status_history_idx_name" {
  value = local.gsi_pointer
}

# Client
output "dynamodb_clients_table_stream_arn" {
  value = try(
    module.dynamodb_table_client_registrations[0].dynamodb_table_stream_arn,
    data.aws_dynamodb_table.dynamodb_table_client_registrations[0].stream_arn
  )
}

# IDP Metadata
output "table_idp_metadata_name" {
  value = try(module.dynamodb_table_idpMetadata[0].dynamodb_table_id, null)
}

output "table_idp_metadata_idx_name" {
  value = local.gsi_pointer
}

output "table_idp_metadata_arn" {
  value = try(module.dynamodb_table_idpMetadata[0].dynamodb_table_arn, null)
}

output "table_idpMetadata_gsi_pointer_arn" {
  value = try("${module.dynamodb_table_idpMetadata[0].dynamodb_table_arn}/index/${local.gsi_pointer}", null)
}

# Last IDP Used
output "table_last_idp_used_arn" {
  value = try(
    module.dynamodb_table_last_idp_used[0].dynamodb_table_arn,
    data.aws_dynamodb_table.dynamodb_table_last_idp_used[0].arn
  )
}
output "table_client_registrations_stream_label" {
  value = try(module.dynamodb_table_client_registrations[0].dynamodb_table_stream_label, null)
}

# Internal IDP Users arn
output "internal_idp_users_arn" {
  value = try(module.dynamodb_table_internal_idp_users[0].dynamodb_table_arn, null)
}

output "internal_idp_users_gsi_namespace_arn" {
  value = try("${module.dynamodb_table_internal_idp_users[0].dynamodb_table_arn}/index/${local.gsi_namespace}", null)
}

output "internal_idp_users_table_name" {
  value = try(module.dynamodb_table_internal_idp_users[0].dynamodb_table_id, null)
}

output "internal_idp_users_gsi_namespace_name" {
  value = local.gsi_namespace
}

