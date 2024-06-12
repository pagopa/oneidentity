output "ecr_endpoints" {
  value = [for r in module.ecr.* : r]
}

output "ecs_cluster_name" {
  value = module.ecs.cluster_name
}

output "ecs_deploy_iam_role_arn" {
  value = aws_iam_role.githubecsdeploy.arn
}

output "lambda_deploy_iam_role_arn" {
  value = aws_iam_policy.deploy_lambda.arn
}

output "jwt_sign_aliases" {
  value = module.jwt_sign.aliases
}

## Network loadbalancer ##
output "nlb_arn" {
  value = module.elb.arn
}

output "nlb_dns_name" {
  value = module.elb.dns_name
}