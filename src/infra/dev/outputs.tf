

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

## ALB ##

/*
output "alb_dns_name" {
  value = module.alb.dns_name
}
*/

## ACM
output "acm_certificate_validation_domains" {
  value = module.frontend.acm_validation_domains
}

## ApiGw

/*
output "rest_api_v1_invoke_url" {
  value = module.poc_v1.rest_api_invoke_url
}
*/

output "rest_api_v2_invoke_url" {
  value = module.poc_v2.rest_api_invoke_url
}

output "dns_zone_name_servers" {
  value = module.frontend.route53_zone_name_servers
}


# Storage
output "assertions_bucket_name" {
  value = module.storage.assertions_bucket_name
}

output "assertions_bucket_arn" {
  value = module.storage.assertions_bucket_arn
}

# Database
output "table_saml_responses_name" {
  value = module.database.saml_responses_table_name
}

