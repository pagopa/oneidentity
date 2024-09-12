## ECS ##
output "ecs_cluster_name" {
  value = module.backend.ecs_cluster_name
}

output "ecr_endpoints" {
  value = module.backend.ecr_endpoints
}

output "ecs_deploy_iam_role_arn" {
  value = module.backend.ecs_deploy_iam_role_arn
}

## ACM
output "acm_certificate_validation_domains" {
  value = module.frontend.acm_validation_domains
}

## ApiGw

output "rest_api_invoke_url" {
  value = module.frontend.rest_api_invoke_url
}

## DNS ##
output "dns_zone_name_servers" {
  value = module.r53_zones.dns_zone_name_servers
}

# Storage
output "assertions_bucket_name" {
  value = module.storage.assertions_bucket_name
}

output "assertions_bucket_arn" {
  value = module.storage.assertions_bucket_arn
}

output "kms_assertion_key_arn" {
  value = module.storage.kms_assertion_key_arn
}

output "deploy_assets_role" {
  value = module.storage.deploy_assets_role
}

output "assets_bucket_name" {
  value = module.storage.assets_bucket_name
}

# Database
output "table_saml_responses_name" {
  value = module.database.table_sessions_name
}

output "table_client_registrations_name" {
  value = module.database.table_client_registrations_name
}

## Spid validator ##
output "spid_validator_fqdn" {
  value = module.spid_validator.route53_record_fqdn
}

output "spid_validator_ecr_endpoint" {
  value = module.spid_validator.ecr_endpoint
}
