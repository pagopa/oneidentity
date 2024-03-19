output "rest_api_id" {
  value = aws_api_gateway_rest_api.main.id
}

output "rest_api_invoke_url" {
  value = aws_api_gateway_stage.main.invoke_url
}

output "domain_name" {
  value = aws_api_gateway_domain_name.main[0].domain_name
}

output "regional_domain_name" {
  value = aws_api_gateway_domain_name.main[0].regional_domain_name
}

output "regional_zone_id" {
  value = aws_api_gateway_domain_name.main[0].regional_zone_id
}