output "table_sessions_name" {
  value = module.sessions_table.dynamodb_table_id
}

output "table_sessions_arn" {
  value = module.sessions_table.dynamodb_table_arn
}

output "table_sessions_gsi_code_arn" {
  value = "${module.sessions_table.dynamodb_table_arn}/index/${local.gsi_code}"
}

output "table_client_registrations_name" {
  value = module.sessions_table.dynamodb_table_id
}

output "table_client_registrations_arn" {
  value = module.dynamodb_table_client_registrations.dynamodb_table_arn
}

output "kms_sessions_table_alias_arn" {
  value = module.kms_sessions_table.aliases[local.kms_sessions_table_alias].target_key_arn
}

output "dynamodb_table_stream_arn" {
  value = module.sessions_table.dynamodb_table_stream_arn
}
