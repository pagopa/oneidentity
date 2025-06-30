## ACM ##
output "acm_validation_domains" {
  value = module.acm.validation_domains
}

output "acm_certificate_arn" {
  value = module.acm.acm_certificate_arn
}

output "acm_auth_certificate_arn" {
  value = try(aws_acm_certificate_validation.auth[0].certificate_arn, null)
}


output "acm_domain_name" {
  value = try(aws_acm_certificate.auth[0].domain_name, null)
}

output "rest_api_invoke_url" {
  value = module.rest_api.rest_api_invoke_url
}

output "api_name" {
  value = module.rest_api.rest_api_name
}

output "rest_api_id" {
  value = module.rest_api.rest_api_id
}

output "rest_api_execution_arn" {
  value = module.rest_api.rest_api_execution_arn
}

output "rest_api_stage_name" {
  value = module.rest_api.rest_api_stage_name
}

output "rest_api_arn" {
  value = module.rest_api.rest_api_arn
}