output "vpc_id" {
  value = module.vpc.vpc_id

}

output "vpc_cidr_block" {
  value = module.vpc.vpc_cidr_block
}

output "public_subnet_ids" {
  value = module.vpc.public_subnets
}

output "private_subnet_ids" {
  value = module.vpc.private_subnets
}


# DNS #
output "route53_zone_name_servers" {
  value = module.zones.route53_zone_name_servers
}

output "route53_zone_zone_id" {
  value = module.zones.route53_zone_zone_id
}


## ACM ## 
output "acm_validation_domains" {
  value = module.acm.validation_domains
}

output "acm_certificate_arn" {
  value = module.acm.acm_certificate_arn
}
