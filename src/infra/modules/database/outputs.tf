output "table_sessions_name" {
  value = module.dynamodb_sessions_table.dynamodb_table_id
}

output "table_sessions_arn" {
  value = module.dynamodb_sessions_table.dynamodb_table_arn
}

output "table_client_registrations_name" {
  value = module.dynamodb_sessions_table.dynamodb_table_id
}

output "table_client_registrations_arn" {
  value = module.dynamodb_table_client_registrations.dynamodb_table_arn
}