## DNS ##
r53_dns_zone = {
  name    = "dev.oneid.pagopa.it"
  comment = "Oneidentity dev zone."
}

dns_record_ttl = 3600


## ECR ## 
repository_image_tag_mutability = "MUTABLE"


## ECS ##

ecs_oneid_core = {
  image_version    = "0cca0e54ae102384261a464f2cc88fb6b6c091c7"
  cpu              = 512
  memory           = 1024
  container_cpu    = 512
  container_memory = 1024
  autoscaling = object({
    enable       = true
    min_capacity = 1
    max_capacity = 2
  })
}



# Ref: https://pagopa.atlassian.net/wiki/spaces/DEVOPS/pages/132810155/Azure+-+Naming+Tagging+Convention#Tagging
tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "Oneidentity"
  Source      = "https://github.com/pagopa/oneidentity"
  CostCenter  = "tier0"
}