output "ecr_endpoint" {
  value = module.ecr.repository_url
}

output "route53_record_fqdn" {
  value = module.record.route53_record_fqdn
}