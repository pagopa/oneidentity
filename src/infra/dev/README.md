## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | 1.7.4 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38.0 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 5.38.0 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_backend"></a> [backend](#module\_backend) | ../modules/backend | n/a |
| <a name="module_database"></a> [database](#module\_database) | ../modules/database | n/a |
| <a name="module_frontend"></a> [frontend](#module\_frontend) | ../modules/frontend | n/a |
| <a name="module_iam"></a> [iam](#module\_iam) | ../modules/iam | n/a |
| <a name="module_network"></a> [network](#module\_network) | ../modules/network | n/a |
| <a name="module_storage"></a> [storage](#module\_storage) | ../modules/storage | n/a |

## Resources

| Name | Type |
|------|------|
| [aws_caller_identity.current](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/data-sources/caller_identity) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_api_cache_cluster_enabled"></a> [api\_cache\_cluster\_enabled](#input\_api\_cache\_cluster\_enabled) | Enablr cache cluster is enabled for the stage. | `bool` | `true` | no |
| <a name="input_api_cache_cluster_size"></a> [api\_cache\_cluster\_size](#input\_api\_cache\_cluster\_size) | Size of the cache cluster for the stage, if enabled. | `number` | `0.5` | no |
| <a name="input_api_method_settings"></a> [api\_method\_settings](#input\_api\_method\_settings) | List of Api Gateway method settings. | <pre>list(object({<br>    method_path                             = string<br>    metrics_enabled                         = optional(bool, false)<br>    logging_level                           = optional(string, "OFF")<br>    data_trace_enabled                      = optional(bool, false)<br>    throttling_rate_limit                   = optional(number, -1)<br>    throttling_burst_limit                  = optional(number, -1)<br>    caching_enabled                         = optional(bool, false)<br>    cache_ttl_in_seconds                    = optional(number, 0)<br>    cache_data_encrypted                    = optional(bool, false)<br>    require_authorization_for_cache_control = optional(bool, false)<br>    cache_key_parameters                    = optional(list(string), [])<br>  }))</pre> | <pre>[<br>  {<br>    "logging_level": "INFO",<br>    "method_path": "*/*",<br>    "metrics_enabled": true<br>  },<br>  {<br>    "cache_ttl_in_seconds": 3600,<br>    "caching_enabled": true,<br>    "method_path": "static/*/GET"<br>  }<br>]</pre> | no |
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | App name. | `string` | `"oneid"` | no |
| <a name="input_assertion_bucket"></a> [assertion\_bucket](#input\_assertion\_bucket) | Assetion storage configurations. | <pre>object({<br>    mfa_delete               = bool<br>    glacier_transaction_days = number<br>    expiration_days          = number<br>  })</pre> | <pre>{<br>  "expiration_days": 100,<br>  "glacier_transaction_days": 90,<br>  "mfa_delete": false<br>}</pre> | no |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS region to create resources. Default Milan | `string` | `"eu-south-1"` | no |
| <a name="input_aws_region_short"></a> [aws\_region\_short](#input\_aws\_region\_short) | AWS region short format. | `string` | `"es-1"` | no |
| <a name="input_client_registrations_table"></a> [client\_registrations\_table](#input\_client\_registrations\_table) | Client configurations. | <pre>object({<br>    point_in_time_recovery_enabled = optional(bool, false)<br>  })</pre> | <pre>{<br>  "point_in_time_recovery_enabled": false<br>}</pre> | no |
| <a name="input_dns_record_ttl"></a> [dns\_record\_ttl](#input\_dns\_record\_ttl) | Dns record ttl (in sec) | `number` | `3600` | no |
| <a name="input_ecr_keep_images"></a> [ecr\_keep\_images](#input\_ecr\_keep\_images) | Number of images to keep. | `number` | `3` | no |
| <a name="input_ecs_enable_container_insights"></a> [ecs\_enable\_container\_insights](#input\_ecs\_enable\_container\_insights) | Enable ecs cluster container inight. | `bool` | `false` | no |
| <a name="input_ecs_oneid_core"></a> [ecs\_oneid\_core](#input\_ecs\_oneid\_core) | Oneidentity core backend configurations. | <pre>object({<br>    image_version    = string<br>    cpu              = number<br>    memory           = number<br>    container_cpu    = number<br>    container_memory = number<br>    autoscaling = object({<br>      enable       = bool<br>      min_capacity = number<br>      max_capacity = number<br>    })<br>    app_spid_test_enabled = optional(bool, false)<br>  })</pre> | <pre>{<br>  "app_spid_test_enabled": true,<br>  "autoscaling": {<br>    "enable": true,<br>    "max_capacity": 2,<br>    "min_capacity": 1<br>  },<br>  "container_cpu": 512,<br>  "container_memory": 1024,<br>  "cpu": 512,<br>  "image_version": "0bfd81912534495aad0bb8cac3bf1f5aeb763625",<br>  "memory": 1024<br>}</pre> | no |
| <a name="input_enable_nat_gateway"></a> [enable\_nat\_gateway](#input\_enable\_nat\_gateway) | Create nat gateway(s) | `bool` | `true` | no |
| <a name="input_env_short"></a> [env\_short](#input\_env\_short) | Evnironment short. | `string` | `"d"` | no |
| <a name="input_number_of_images_to_keep"></a> [number\_of\_images\_to\_keep](#input\_number\_of\_images\_to\_keep) | Number of images to keeps in ECR. | `number` | `3` | no |
| <a name="input_r53_dns_zone"></a> [r53\_dns\_zone](#input\_r53\_dns\_zone) | # R53 DNS zone ## | <pre>object({<br>    name    = string<br>    comment = string<br>  })</pre> | <pre>{<br>  "comment": "Oneidentity dev zone.",<br>  "name": "dev.oneid.pagopa.it"<br>}</pre> | no |
| <a name="input_repository_image_tag_mutability"></a> [repository\_image\_tag\_mutability](#input\_repository\_image\_tag\_mutability) | The tag mutability setting for the repository. Must be one of: MUTABLE or IMMUTABLE. Defaults to IMMUTABLE | `string` | `"MUTABLE"` | no |
| <a name="input_rest_api_throttle_settings"></a> [rest\_api\_throttle\_settings](#input\_rest\_api\_throttle\_settings) | Rest api throttle settings. | <pre>object({<br>    burst_limit = number<br>    rate_limit  = number<br>  })</pre> | <pre>{<br>  "burst_limit": 100,<br>  "rate_limit": 50<br>}</pre> | no |
| <a name="input_sessions_table"></a> [sessions\_table](#input\_sessions\_table) | Saml responses table configurations. | <pre>object({<br>    ttl_enabled                    = bool<br>    point_in_time_recovery_enabled = bool<br>    stream_enabled                 = bool<br>    stream_view_type               = string<br>  })</pre> | <pre>{<br>  "point_in_time_recovery_enabled": false,<br>  "stream_enabled": true,<br>  "stream_view_type": "NEW_AND_OLD_IMAGES",<br>  "ttl_enabled": true<br>}</pre> | no |
| <a name="input_single_nat_gateway"></a> [single\_nat\_gateway](#input\_single\_nat\_gateway) | Create a single nat gateway to spare money. | `bool` | `true` | no |
| <a name="input_tags"></a> [tags](#input\_tags) | n/a | `map(any)` | <pre>{<br>  "CostCenter": "tier0",<br>  "CreatedBy": "Terraform",<br>  "Environment": "Dev",<br>  "Owner": "Oneidentity",<br>  "Source": "https://github.com/pagopa/oneidentity"<br>}</pre> | no |
| <a name="input_vpc_cidr"></a> [vpc\_cidr](#input\_vpc\_cidr) | VPC address space | `string` | `"10.0.0.0/17"` | no |
| <a name="input_vpc_internal_subnets_cidr"></a> [vpc\_internal\_subnets\_cidr](#input\_vpc\_internal\_subnets\_cidr) | Internal subnets address spaces. | `list(string)` | <pre>[<br>  "10.0.32.0/20",<br>  "10.0.16.0/20",<br>  "10.0.0.0/20"<br>]</pre> | no |
| <a name="input_vpc_private_subnets_cidr"></a> [vpc\_private\_subnets\_cidr](#input\_vpc\_private\_subnets\_cidr) | Private subnets address spaces. | `list(string)` | <pre>[<br>  "10.0.80.0/20",<br>  "10.0.64.0/20",<br>  "10.0.48.0/20"<br>]</pre> | no |
| <a name="input_vpc_public_subnets_cidr"></a> [vpc\_public\_subnets\_cidr](#input\_vpc\_public\_subnets\_cidr) | Public subnets address spaces. | `list(string)` | <pre>[<br>  "10.0.120.0/21",<br>  "10.0.112.0/21",<br>  "10.0.104.0/21"<br>]</pre> | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_acm_certificate_validation_domains"></a> [acm\_certificate\_validation\_domains](#output\_acm\_certificate\_validation\_domains) | # ACM |
| <a name="output_assertions_bucket_arn"></a> [assertions\_bucket\_arn](#output\_assertions\_bucket\_arn) | n/a |
| <a name="output_assertions_bucket_name"></a> [assertions\_bucket\_name](#output\_assertions\_bucket\_name) | Storage |
| <a name="output_dns_zone_name_servers"></a> [dns\_zone\_name\_servers](#output\_dns\_zone\_name\_servers) | # DNS ## |
| <a name="output_ecr_endpoints"></a> [ecr\_endpoints](#output\_ecr\_endpoints) | n/a |
| <a name="output_ecs_cluster_name"></a> [ecs\_cluster\_name](#output\_ecs\_cluster\_name) | # ECS ## |
| <a name="output_ecs_deploy_iam_role_arn"></a> [ecs\_deploy\_iam\_role\_arn](#output\_ecs\_deploy\_iam\_role\_arn) | n/a |
| <a name="output_rest_api_invoke_url"></a> [rest\_api\_invoke\_url](#output\_rest\_api\_invoke\_url) | n/a |
| <a name="output_table_client_registrations_name"></a> [table\_client\_registrations\_name](#output\_table\_client\_registrations\_name) | n/a |
| <a name="output_table_saml_responses_name"></a> [table\_saml\_responses\_name](#output\_table\_saml\_responses\_name) | Database |
