output "saml_responses_table_name" {
  value = module.dynamodb_table_saml_responses.dynamodb_table_id
}

output "saml_responses_table_arn" {
  value = module.dynamodb_table_saml_responses.dynamodb_table_arn
}