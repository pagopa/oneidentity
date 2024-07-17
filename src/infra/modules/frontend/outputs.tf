
## DNS ##
output "route53_zone_name_servers" {
  value = module.zones.route53_zone_name_servers
}

output "zone_name" {
  value = keys(module.zones.route53_zone_zone_id)[0]
}

output "route53_zone_id" {
  value = module.zones.route53_zone_zone_id[
    keys(module.zones.route53_zone_zone_id)[0]
  ]
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

output "api_name" {
  value = module.rest_api.rest_api_name
}