## DNS ##
r53_dns_zone = {
  name    = "dev.oneid.pagopa.it"
  comment = "Oneidentity dev zone."
}

dns_record_ttl = 3600


## ECR ## 
repository_image_tag_mutability = "MUTABLE"


## ECS ##

idp_image_version = "56.0"

ecs_autoscaling_idp = {
  enable       = true
  min_capacity = 1
  max_capacity = 2
}


# Ref: https://pagopa.atlassian.net/wiki/spaces/DEVOPS/pages/132810155/Azure+-+Naming+Tagging+Convention#Tagging
tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "Oneidentity"
  Source      = "https://github.com/pagopa/oneidentity"
  CostCenter  = "tier0"
}