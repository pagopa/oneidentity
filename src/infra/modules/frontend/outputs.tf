## ACM ##
output "acm_validation_domains" {
  value = module.acm.validation_domains
}

output "acm_certificate_arn" {
  value = module.acm.acm_certificate_arn
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