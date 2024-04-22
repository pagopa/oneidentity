output "saml_responses_table_name" {
  value = module.dynamodb_table_saml_responses.dynamodb_table_id
}