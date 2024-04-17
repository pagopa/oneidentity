## DNS ##
r53_dns_zone = {
  name    = "dev.oneidentity.pagopa.it"
  comment = "Oneidentity dev zone."
}

dns_record_ttl = 3600


## ECS ##
poc1_image_version = "1.0"
poc2_image_version = "1.0"


# Ref: https://pagopa.atlassian.net/wiki/spaces/DEVOPS/pages/132810155/Azure+-+Naming+Tagging+Convention#Tagging
tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "Oneidentity"
  Source      = "https://github.com/pagopa/oneidentity"
  CostCenter  = "tier0"
}