output "ecr_endpoints" {
  value = [for r in module.ecr.* : r]
}

output "ecs_cluster_name" {
  value = module.ecs.cluster_name
}

output "ecs_deploy_iam_role_arn" {
  value = aws_iam_role.githubecsdeploy.arn
}


output "jwt_sign_aliases" {
  value = module.jwt_sign.aliases
}