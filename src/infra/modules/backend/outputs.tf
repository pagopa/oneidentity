output "ecr_endpoints" {
  value = [for r in module.ecr.* : r]
}

output "ecs_cluster_name" {
  value = module.ecs_cluster.cluster_name
}

output "ecs_cluster_arn" {
  value = module.ecs_cluster.cluster_arn
}

output "ecs_service_name" {
  value = module.ecs_core_service.name
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

output "nlb_arn_suffix" {
  value = module.elb.arn_suffix
}

output "nlb_target_group_suffix_arn" {
  value = module.elb.target_groups["ecs-oneid-core"].arn_suffix
}

output "nlb_dns_name" {
  value = module.elb.dns_name
}

output "elb" {
  value = module.elb
}

#TODO get the name from the arn
output "metadata_lambda_name" {
  value = module.metadata_lambda.lambda_function_name
}

output "metadata_lambda_arn" {
  value = module.metadata_lambda.lambda_function_arn
}

output "metadata_lambda_log_group_name" {
  value = module.metadata_lambda.lambda_cloudwatch_log_group_name
}

## Client registration lambda
output "client_registration_lambda_arn" {
  value = module.client_registration_lambda.lambda_function_arn
}

## Client manager lambda
output "client_manager_lambda_arn" {
  value = module.client_manager_lambda[0].lambda_function_arn
}

output "client_registration_log_group_name" {
  value = module.client_registration_lambda.lambda_cloudwatch_log_group_name
}
## Retrieve status lambda
output "retrieve_status_lambda_arn" {
  value = module.retrieve_status_lambda.lambda_function_arn
}

## Metadata lambda ##
output "assertion_lambda_arn" {
  value = module.assertion_lambda.lambda_function_arn
}


## S3 buket for lambda code ##
output "s3_lambda_code_bucket" {
  value = module.s3_lambda_code_bucket.s3_bucket_id
}

output "ecs_core_log_group_name" {
  value = aws_cloudwatch_log_group.ecs_core.name
}
