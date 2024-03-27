

## ECS ##
output "ecs_cluster_name" {
  value = module.ecs.cluster_name
}

output "ecr_repository_url" {
  value = module.ecr.repository_url
}

## ALB ##
/*
output "alb_dns_name" {
  value = module.alb.dns_name
}
*/

## ACM
output "acm_certificate_validation_domains" {
  value = module.acm.validation_domains
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
  value = module.zones.route53_zone_name_servers
}
