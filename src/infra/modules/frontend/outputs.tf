
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

## ALB Spid Validator ## 

output "spid_validator_alb_dns_name" {
  value = module.alb_spid_validator[0].dns_name  
}

output "spid_validator_alb_arn" {
  value = module.alb_spid_validator[0].arn  
}

output "spid_validator_alb_target_group_arn" {
  value = module.alb_spid_validator[0].target_groups["spid_validator"].arn  
}

output "spid_validator_alb_security_group_id" {
  value = module.alb_spid_validator[0].security_group_id
}