
## DNS ##
output "route53_zone_name_servers" {
  value = module.zones.route53_zone_name_servers
}

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