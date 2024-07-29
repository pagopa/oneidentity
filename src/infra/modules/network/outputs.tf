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

output "intra_subnets_ids" {
  value = module.vpc.intra_subnets
}

output "vpc_endpoints" {
  value = module.vpc_endpoints.endpoints
}
