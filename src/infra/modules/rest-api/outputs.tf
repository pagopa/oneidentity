output "rest_api_id" {
  value = aws_api_gateway_rest_api.main.id
}

output "rest_api_invoke_url" {
  value = aws_api_gateway_stage.main.invoke_url
}