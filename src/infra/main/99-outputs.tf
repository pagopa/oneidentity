

## ECS ##
output "ecs_cluster_name" {
  value = module.ecs.cluster_name
}

output "ecr_repository_url" {
  value = module.ecr.repository_url
}

## ALB ##

output "alb_dns_name" {
  value = module.alb.dns_name
}
