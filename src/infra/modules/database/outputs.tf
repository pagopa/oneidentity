output "table_saml_responses_name" {
  value = module.dynamodb_table_saml_responses.dynamodb_table_id
}

output "table_saml_responses_arn" {
  value = module.dynamodb_table_saml_responses.dynamodb_table_arn
}

output "table_client_registrations_name" {
  value = module.dynamodb_table_client_registrations.dynamodb_table_id
}

output "table_client_registrations_arn" {
  value = module.dynamodb_table_client_registrations.dynamodb_table_arn
}