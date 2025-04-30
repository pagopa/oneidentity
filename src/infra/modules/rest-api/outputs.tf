output "rest_api_id" {
  value = aws_api_gateway_rest_api.main.id
}

output "rest_api_invoke_url" {
  value = aws_api_gateway_stage.main.invoke_url
}

output "domain_name" {
  value = try(aws_api_gateway_domain_name.main[0].domain_name, null)
}

output "regional_domain_name" {
  value = try(aws_api_gateway_domain_name.main[0].regional_domain_name, null)
}

output "regional_zone_id" {
  value = try(aws_api_gateway_domain_name.main[0].regional_zone_id, null)
}

output "rest_api_execution_arn" {
  value = aws_api_gateway_rest_api.main.execution_arn
}

output "rest_api_name" {
  value = aws_api_gateway_rest_api.main.name
}

output "rest_api_stage_name" {
  value = aws_api_gateway_stage.main.stage_name
}

output "rest_api_arn" {
  value = aws_api_gateway_rest_api.main.arn
}