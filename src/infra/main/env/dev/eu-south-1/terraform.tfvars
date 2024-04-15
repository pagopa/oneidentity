env_short        = "d"
environment      = "dev"
aws_region_short = "es-1"

# https://www.davidc.net/sites/default/subnets/subnets.html?network=10.0.0.0&mask=17&division=19.72331

vpc_private_subnets_cidr  = ["10.0.80.0/20", "10.0.64.0/20", "10.0.48.0/20"]
vpc_public_subnets_cidr   = ["10.0.120.0/21", "10.0.112.0/21", "10.0.104.0/21"]
vpc_internal_subnets_cidr = ["10.0.32.0/20", "10.0.16.0/20", "10.0.0.0/20"]
enable_nat_gateway        = false
single_nat_gateway        = true


## DNS ##
r53_dns_zone = {
  name    = "dev.oneidentity.pagopa.it"
  comment = "Oneidentity dev zone."
}

dns_record_ttl = 3600


# Ref: https://pagopa.atlassian.net/wiki/spaces/DEVOPS/pages/132810155/Azure+-+Naming+Tagging+Convention#Tagging
tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "Oneidentity"
  Source      = "https://github.com/pagopa/oneidentity"
  CostCenter  = "tier0"
}