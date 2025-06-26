output "user_pool_arn" {
  value = aws_cognito_user_pool.main.arn
}

output "user_pool_id" {
  value = aws_cognito_user_pool.main.id
}

output "cloudfront_distribution" {
  value = aws_cognito_user_pool_domain.auth.cloudfront_distribution
}

output "cloudfront_distribution_zone_id" {
  value = aws_cognito_user_pool_domain.auth.cloudfront_distribution_zone_id
}
