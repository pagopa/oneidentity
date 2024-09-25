output "table_sessions_name" {
  value = module.dynamodb_sessions_table.dynamodb_table_id
}

output "table_sessions_arn" {
  value = module.dynamodb_sessions_table.dynamodb_table_arn
}

output "table_sessions_gsi_code_arn" {
  value = "${module.dynamodb_sessions_table.dynamodb_table_arn}/index/${local.gsi_code}"
}

output "table_client_registrations_name" {
  value = module.dynamodb_table_client_registrations[*].dynamodb_table_id
}

output "table_client_registrations_arn" {
  value = module.dynamodb_table_client_registrations[*].dynamodb_table_arn
}

output "kms_sessions_table_alias_arn" {
  value = module.kms_sessions_table.aliases[local.kms_sessions_table_alias].target_key_arn
}

output "dynamodb_table_stream_arn" {
  value = module.dynamodb_sessions_table.dynamodb_table_stream_arn
}

output "table_idp_metadata_name" {
  value = module.dynamodb_table_idpMetadata.dynamodb_table_id
}

output "table_idp_metadata_idx_name" {
  value = local.gsi_pointer
}

output "table_idp_metadata_arn" {
  value = module.dynamodb_table_idpMetadata.dynamodb_table_arn
}

output "table_idpMetadata_gsi_pointer_arn" {
  value = "${module.dynamodb_table_idpMetadata.dynamodb_table_arn}/index/${local.gsi_pointer}"
}

