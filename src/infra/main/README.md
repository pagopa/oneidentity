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
| <a name="module_acm"></a> [acm](#module\_acm) | terraform-aws-modules/acm/aws | 5.0.0 |
| <a name="module_alb"></a> [alb](#module\_alb) | terraform-aws-modules/alb/aws | 9.7.0 |
| <a name="module_ecr"></a> [ecr](#module\_ecr) | terraform-aws-modules/ecr/aws | 1.6.0 |
| <a name="module_ecs"></a> [ecs](#module\_ecs) | terraform-aws-modules/ecs/aws | 5.9.1 |
| <a name="module_ecs_service_poc1"></a> [ecs\_service\_poc1](#module\_ecs\_service\_poc1) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |
| <a name="module_ecs_service_poc2"></a> [ecs\_service\_poc2](#module\_ecs\_service\_poc2) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |
| <a name="module_elb"></a> [elb](#module\_elb) | terraform-aws-modules/alb/aws | 9.8.0 |
| <a name="module_poc_v2"></a> [poc\_v2](#module\_poc\_v2) | ../modules/rest-api | n/a |
| <a name="module_vpc"></a> [vpc](#module\_vpc) | terraform-aws-modules/vpc/aws | 5.5.2 |
| <a name="module_vpc_endpoints"></a> [vpc\_endpoints](#module\_vpc\_endpoints) | terraform-aws-modules/vpc/aws//modules/vpc-endpoints | 5.5.2 |
| <a name="module_zones"></a> [zones](#module\_zones) | terraform-aws-modules/route53/aws//modules/zones | 2.11.0 |

## Resources

| Name | Type |
|------|------|
| [aws_api_gateway_vpc_link.apigw](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/resources/api_gateway_vpc_link) | resource |
| [aws_route53_record.dev](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/resources/route53_record) | resource |
| [aws_route53_record.main](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/resources/route53_record) | resource |
| [aws_security_group.vpc_tls](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/resources/security_group) | resource |
| [aws_security_group_rule.allow_all_https_poc2](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/resources/security_group_rule) | resource |
| [aws_security_group.default](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/data-sources/security_group) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_r53_dns_zone"></a> [r53\_dns\_zone](#input\_r53\_dns\_zone) | # R53 DNS zone ## | <pre>object({<br>    name    = string<br>    comment = string<br>  })</pre> | n/a | yes |
| <a name="input_vpc_internal_subnets_cidr"></a> [vpc\_internal\_subnets\_cidr](#input\_vpc\_internal\_subnets\_cidr) | Internal subnets list of cidr. Mainly for private endpoints | `list(string)` | n/a | yes |
| <a name="input_vpc_private_subnets_cidr"></a> [vpc\_private\_subnets\_cidr](#input\_vpc\_private\_subnets\_cidr) | Private subnets list of cidr. | `list(string)` | n/a | yes |
| <a name="input_vpc_public_subnets_cidr"></a> [vpc\_public\_subnets\_cidr](#input\_vpc\_public\_subnets\_cidr) | Private subnets list of cidr. | `list(string)` | n/a | yes |
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | App name. | `string` | `"oneidentity"` | no |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS region to create resources. Default Milan | `string` | `"eu-south-1"` | no |
| <a name="input_azs"></a> [azs](#input\_azs) | Availability zones | `list(string)` | <pre>[<br>  "eu-south-1a",<br>  "eu-south-1b",<br>  "eu-south-1c"<br>]</pre> | no |
| <a name="input_dns_record_ttl"></a> [dns\_record\_ttl](#input\_dns\_record\_ttl) | Dns record ttl (in sec) | `number` | `86400` | no |
| <a name="input_ecr_keep_images"></a> [ecr\_keep\_images](#input\_ecr\_keep\_images) | Number of images to keep. | `number` | `3` | no |
| <a name="input_ecs_autoscaling_poc1"></a> [ecs\_autoscaling\_poc1](#input\_ecs\_autoscaling\_poc1) | n/a | <pre>object({<br>    enable_autoscaling       = bool<br>    autoscaling_min_capacity = number<br>    autoscaling_max_capacity = number<br>  })</pre> | <pre>{<br>  "autoscaling_max_capacity": 3,<br>  "autoscaling_min_capacity": 1,<br>  "enable_autoscaling": true<br>}</pre> | no |
| <a name="input_ecs_enable_container_insights"></a> [ecs\_enable\_container\_insights](#input\_ecs\_enable\_container\_insights) | Enable ecs cluster container inight. | `bool` | `false` | no |
| <a name="input_enable_nat_gateway"></a> [enable\_nat\_gateway](#input\_enable\_nat\_gateway) | Enable/Create nat gateway | `bool` | `false` | no |
| <a name="input_env_short"></a> [env\_short](#input\_env\_short) | Evnironment short. | `string` | `"d"` | no |
| <a name="input_single_nat_gateway"></a> [single\_nat\_gateway](#input\_single\_nat\_gateway) | Create just one natgateway | `bool` | `false` | no |
| <a name="input_tags"></a> [tags](#input\_tags) | n/a | `map(any)` | <pre>{<br>  "CreatedBy": "Terraform"<br>}</pre> | no |
| <a name="input_vpc_cidr"></a> [vpc\_cidr](#input\_vpc\_cidr) | VPC cidr. | `string` | `"10.0.0.0/17"` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_acm_certificate_validation_domains"></a> [acm\_certificate\_validation\_domains](#output\_acm\_certificate\_validation\_domains) | # ACM |
| <a name="output_dns_zone_name_servers"></a> [dns\_zone\_name\_servers](#output\_dns\_zone\_name\_servers) | n/a |
| <a name="output_ecr_repository_url"></a> [ecr\_repository\_url](#output\_ecr\_repository\_url) | n/a |
| <a name="output_ecs_cluster_name"></a> [ecs\_cluster\_name](#output\_ecs\_cluster\_name) | # ECS ## |
| <a name="output_rest_api_v2_invoke_url"></a> [rest\_api\_v2\_invoke\_url](#output\_rest\_api\_v2\_invoke\_url) | n/a |
