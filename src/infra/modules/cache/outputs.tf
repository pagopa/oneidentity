output "cache_name" {
  value = aws_elasticache_serverless_cache.client_cache.name
}

output "cache_arn" {
  value = aws_elasticache_serverless_cache.client_cache.arn
}

output "cache_endpoint_address" {
  value = aws_elasticache_serverless_cache.client_cache.endpoint[0].address
}

output "cache_endpoint_port" {
  value = aws_elasticache_serverless_cache.client_cache.endpoint[0].port
}

output "cache_security_group_id" {
  value = aws_security_group.client_cache.id
}
