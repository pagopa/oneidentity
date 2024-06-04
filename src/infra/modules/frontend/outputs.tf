output "alb_dns_name" {
  value = module.alb.dns_name
}

output "alb_target_groups" {
  value = module.alb.target_groups
}

output "alb_security_group_id" {
  value = module.alb.security_group_id
}

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