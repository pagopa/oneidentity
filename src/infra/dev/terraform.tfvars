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
  image_version    = "0bfd81912534495aad0bb8cac3bf1f5aeb763625"
  cpu              = 512
  memory           = 1024
  container_cpu    = 512
  container_memory = 1024
  autoscaling = {
    enable       = true
    min_capacity = 1
    max_capacity = 2
  }
  app_spid_test_enabled = true
}

## Api Gateway ##
rest_api_throttle_settings = {
  rate_limit  = 50
  burst_limit = 100
}

api_cache_cluster_enabled = true



# Ref: https://pagopa.atlassian.net/wiki/spaces/DEVOPS/pages/132810155/Azure+-+Naming+Tagging+Convention#Tagging
tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "Oneidentity"
  Source      = "https://github.com/pagopa/oneidentity"
  CostCenter  = "tier0"
}