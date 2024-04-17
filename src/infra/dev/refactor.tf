moved {
  from = module.ecs_service
  to   = module.ecs_service_poc1
}

moved {
  from = module.vpc_endpoints.aws_vpc_endpoint.this["ecr_dkr"]
  to   = module.network.module.vpc_endpoints.aws_vpc_endpoint.this["ecr_dkr"]
}

moved {
  from = module.vpc_endpoints.aws_vpc_endpoint.this["s3"]
  to   = module.network.module.vpc_endpoints.aws_vpc_endpoint.this["s3"]
}


moved {
  from = module.vpc_endpoints.aws_vpc_endpoint.this["logs"]
  to   = module.network.module.vpc_endpoints.aws_vpc_endpoint.this["logs"]
}

moved {
  from = aws_security_group.vpc_tls
  to   = module.network.aws_security_group.vpc_tls
}

moved {
  from = module.vpc.aws_default_network_acl.this[0]
  to   = module.network.module.vpc.aws_default_network_acl.this[0]
}

moved {
  from = module.vpc.aws_default_route_table.default[0]
  to   = module.network.module.vpc.aws_default_route_table.default[0]
}

moved {
  from = module.vpc.aws_default_security_group.this[0]
  to   = module.network.module.vpc.aws_default_security_group.this[0]
}

moved {
  from = module.vpc.aws_internet_gateway.this[0]
  to   = module.network.module.vpc.aws_internet_gateway.this[0]
}

moved {
  from = module.vpc.aws_route.public_internet_gateway[0]
  to   = module.network.module.vpc.aws_route.public_internet_gateway[0]
}

moved {
  from = module.vpc.aws_route_table.intra[0]
  to   = module.network.module.vpc.aws_route_table.intra[0]
}

moved {
  from = module.vpc.aws_route_table.private[0]
  to   = module.network.module.vpc.aws_route_table.private[0]
}

moved {
  from = module.vpc.aws_route_table.public[0]
  to   = module.network.module.vpc.aws_route_table.public[0]
}

moved {
  from = module.vpc.aws_route_table_association.intra[0]
  to   = module.network.module.vpc.aws_route_table_association.intra[0]
}

moved {
  from = module.vpc.aws_route_table_association.intra[1]
  to   = module.network.module.vpc.aws_route_table_association.intra[1]
}

moved {
  from = module.vpc.aws_route_table_association.intra[2]
  to   = module.network.module.vpc.aws_route_table_association.intra[2]
}

moved {
  from = module.vpc.aws_route_table_association.private[0]
  to   = module.network.module.vpc.aws_route_table_association.private[0]
}

moved {
  from = module.vpc.aws_route_table_association.private[1]
  to   = module.network.module.vpc.aws_route_table_association.private[1]
}

moved {
  from = module.vpc.aws_route_table_association.private[2]
  to   = module.network.module.vpc.aws_route_table_association.private[2]
}

moved {
  from = module.vpc.aws_route_table_association.public[0]
  to   = module.network.module.vpc.aws_route_table_association.public[0]
}

moved {
  from = module.vpc.aws_route_table_association.public[1]
  to   = module.network.module.vpc.aws_route_table_association.public[1]
}

moved {
  from = module.vpc.aws_route_table_association.public[2]
  to   = module.network.module.vpc.aws_route_table_association.public[2]
}

moved {
  from = module.vpc.aws_subnet.intra[0]
  to   = module.network.module.vpc.aws_subnet.intra[0]
}

moved {
  from = module.vpc.aws_subnet.intra[1]
  to   = module.network.module.vpc.aws_subnet.intra[1]
}

moved {
  from = module.vpc.aws_subnet.intra[2]
  to   = module.network.module.vpc.aws_subnet.intra[2]
}

moved {
  from = module.vpc.aws_subnet.private[0]
  to   = module.network.module.vpc.aws_subnet.private[0]
}

moved {
  from = module.vpc.aws_subnet.private[1]
  to   = module.network.module.vpc.aws_subnet.private[1]
}

moved {
  from = module.vpc.aws_subnet.private[2]
  to   = module.network.module.vpc.aws_subnet.private[2]
}

moved {
  from = module.vpc.aws_subnet.public[0]
  to   = module.network.module.vpc.aws_subnet.public[0]
}

moved {
  from = module.vpc.aws_subnet.public[1]
  to   = module.network.module.vpc.aws_subnet.public[1]
}

moved {
  from = module.vpc.aws_subnet.public[2]
  to   = module.network.module.vpc.aws_subnet.public[2]
}

moved {
  from = module.vpc.aws_vpc.this[0]
  to   = module.network.module.vpc.aws_vpc.this[0]
}

moved {
  from = module.vpc_endpoints.aws_vpc_endpoint.this["ecr_api"]
  to   = module.network.module.vpc_endpoints.aws_vpc_endpoint.this["ecr_api"]
}

moved {
  from = module.records.aws_route53_record.this[" A"]
  to   = module.network.module.records.aws_route53_record.this[" A"]
}


moved {
  from = module.zones.aws_route53_zone.this["dev.oneidentity.pagopa.it"]
  to   = module.network.module.zones.aws_route53_zone.this["dev.oneidentity.pagopa.it"]
}

moved {
  from = module.zones.aws_route53_zone.this["oneidentity.pagopa.it"]
  to   = module.network.module.zones.aws_route53_zone.this["oneidentity.pagopa.it"]
}

moved {
  from = module.acm.aws_acm_certificate.this[0]
  to   = module.network.module.acm.aws_acm_certificate.this[0]
}

moved {
  from = module.acm.aws_acm_certificate_validation.this[0]
  to   = module.network.module.acm.aws_acm_certificate_validation.this[0]

}

moved {
  from = module.acm.aws_route53_record.validation[0]
  to   = module.network.module.acm.aws_route53_record.validation[0]
}
