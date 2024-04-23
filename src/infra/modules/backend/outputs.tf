output "ecr_endpoints" {
  value = [for r in module.ecr.* : r]
}


output "ecs_cluster_name" {
  value = module.ecs.cluster_name
}