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
| <a name="module_alb"></a> [alb](#module\_alb) | terraform-aws-modules/alb/aws | 9.7.0 |
| <a name="module_databae"></a> [databae](#module\_databae) | ../modules/database |  |
| <a name="module_ecr"></a> [ecr](#module\_ecr) | terraform-aws-modules/ecr/aws | 1.6.0 |
| <a name="module_ecs"></a> [ecs](#module\_ecs) | terraform-aws-modules/ecs/aws | 5.9.1 |
| <a name="module_ecs_service_poc1"></a> [ecs\_service\_poc1](#module\_ecs\_service\_poc1) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |
| <a name="module_ecs_service_poc2"></a> [ecs\_service\_poc2](#module\_ecs\_service\_poc2) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |
| <a name="module_elb"></a> [elb](#module\_elb) | terraform-aws-modules/alb/aws | 9.8.0 |
| <a name="module_network"></a> [network](#module\_network) | ../modules/network |  |
| <a name="module_poc_v2"></a> [poc\_v2](#module\_poc\_v2) | ../modules/rest-api |  |
| <a name="module_records_prod"></a> [records\_prod](#module\_records\_prod) | terraform-aws-modules/route53/aws//modules/records | 2.11.0 |
| <a name="module_storage"></a> [storage](#module\_storage) | ../modules/storage |  |

## Resources

| Name | Type |
|------|------|
| [aws_api_gateway_vpc_link.apigw](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/resources/api_gateway_vpc_link) | resource |
| [aws_security_group_rule.allow_all_https_poc2](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/resources/security_group_rule) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_poc1_image_version"></a> [poc1\_image\_version](#input\_poc1\_image\_version) | Image version poc1. | `string` | n/a | yes |
| <a name="input_poc2_image_version"></a> [poc2\_image\_version](#input\_poc2\_image\_version) | Image version poc2. | `string` | n/a | yes |
| <a name="input_r53_dns_zone"></a> [r53\_dns\_zone](#input\_r53\_dns\_zone) | # R53 DNS zone ## | <pre>object({<br>    name    = string<br>    comment = string<br>  })</pre> | n/a | yes |
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | App name. | `string` | `"oneid"` | no |
| <a name="input_assertion_bucket"></a> [assertion\_bucket](#input\_assertion\_bucket) | # Storage S3 ## | <pre>object({<br>    mfa_delete               = bool<br>    gracier_transaction_days = number<br>    expiration_days          = number<br>  })</pre> | <pre>{<br>  "expiration_days": 100,<br>  "gracier_transaction_days": 90,<br>  "mfa_delete": false<br>}</pre> | no |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS region to create resources. Default Milan | `string` | `"eu-south-1"` | no |
| <a name="input_aws_region_short"></a> [aws\_region\_short](#input\_aws\_region\_short) | AWS region short format. | `string` | `"es-1"` | no |
| <a name="input_dns_record_ttl"></a> [dns\_record\_ttl](#input\_dns\_record\_ttl) | Dns record ttl (in sec) | `number` | `86400` | no |
| <a name="input_ecr_keep_images"></a> [ecr\_keep\_images](#input\_ecr\_keep\_images) | Number of images to keep. | `number` | `3` | no |
| <a name="input_ecs_autoscaling_poc1"></a> [ecs\_autoscaling\_poc1](#input\_ecs\_autoscaling\_poc1) | n/a | <pre>object({<br>    enable_autoscaling       = bool<br>    autoscaling_min_capacity = number<br>    autoscaling_max_capacity = number<br>  })</pre> | <pre>{<br>  "autoscaling_max_capacity": 3,<br>  "autoscaling_min_capacity": 1,<br>  "enable_autoscaling": true<br>}</pre> | no |
| <a name="input_ecs_enable_container_insights"></a> [ecs\_enable\_container\_insights](#input\_ecs\_enable\_container\_insights) | Enable ecs cluster container inight. | `bool` | `false` | no |
| <a name="input_env_short"></a> [env\_short](#input\_env\_short) | Evnironment short. | `string` | `"d"` | no |
| <a name="input_table_saml_responses_point_in_time_recovery_enabled"></a> [table\_saml\_responses\_point\_in\_time\_recovery\_enabled](#input\_table\_saml\_responses\_point\_in\_time\_recovery\_enabled) | Enable point in time recovery table saml responses | `bool` | `false` | no |
| <a name="input_tags"></a> [tags](#input\_tags) | n/a | `map(any)` | <pre>{<br>  "CreatedBy": "Terraform"<br>}</pre> | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_acm_certificate_validation_domains"></a> [acm\_certificate\_validation\_domains](#output\_acm\_certificate\_validation\_domains) | # ACM |
| <a name="output_assertions_bucket_name"></a> [assertions\_bucket\_name](#output\_assertions\_bucket\_name) | Storage |
| <a name="output_dns_zone_name_servers"></a> [dns\_zone\_name\_servers](#output\_dns\_zone\_name\_servers) | n/a |
| <a name="output_ecr_repository_url"></a> [ecr\_repository\_url](#output\_ecr\_repository\_url) | n/a |
| <a name="output_ecs_cluster_name"></a> [ecs\_cluster\_name](#output\_ecs\_cluster\_name) | # ECS ## |
| <a name="output_rest_api_v2_invoke_url"></a> [rest\_api\_v2\_invoke\_url](#output\_rest\_api\_v2\_invoke\_url) | n/a |
| <a name="output_table_saml_responses_name"></a> [table\_saml\_responses\_name](#output\_table\_saml\_responses\_name) | Database |
