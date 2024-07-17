## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 5.38 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_acm_validator"></a> [acm\_validator](#module\_acm\_validator) | terraform-aws-modules/acm/aws | 5.0.0 |
| <a name="module_alb"></a> [alb](#module\_alb) | terraform-aws-modules/alb/aws | 9.8.0 |
| <a name="module_ecr"></a> [ecr](#module\_ecr) | terraform-aws-modules/ecr/aws | 1.6.0 |
| <a name="module_ecs_spid_validator"></a> [ecs\_spid\_validator](#module\_ecs\_spid\_validator) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |
| <a name="module_record"></a> [record](#module\_record) | terraform-aws-modules/route53/aws//modules/records | ~> 3.0 |

## Resources

| Name | Type |
|------|------|
| [aws_cloudwatch_log_group.ecs_spid_validator](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_log_group) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_alb_spid_validator_name"></a> [alb\_spid\_validator\_name](#input\_alb\_spid\_validator\_name) | Alb name. | `string` | n/a | yes |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS region. | `string` | n/a | yes |
| <a name="input_ecr_repository_name"></a> [ecr\_repository\_name](#input\_ecr\_repository\_name) | ECR repository name for spid validator. | `string` | n/a | yes |
| <a name="input_private_subnets_ids"></a> [private\_subnets\_ids](#input\_private\_subnets\_ids) | Private subnets id | `list(string)` | n/a | yes |
| <a name="input_public_subnet_ids"></a> [public\_subnet\_ids](#input\_public\_subnet\_ids) | Public subnet ids. | `list(string)` | n/a | yes |
| <a name="input_vpc_cidr_block"></a> [vpc\_cidr\_block](#input\_vpc\_cidr\_block) | VPC cids block. | `string` | n/a | yes |
| <a name="input_vpc_id"></a> [vpc\_id](#input\_vpc\_id) | VPC id | `string` | n/a | yes |
| <a name="input_zone_id"></a> [zone\_id](#input\_zone\_id) | Zone id where to create the validation record for the ACM certificates. | `string` | n/a | yes |
| <a name="input_zone_name"></a> [zone\_name](#input\_zone\_name) | R53 zone name. | `string` | n/a | yes |
| <a name="input_repository_image_tag_mutability"></a> [repository\_image\_tag\_mutability](#input\_repository\_image\_tag\_mutability) | The tag mutability setting for the repository. Must be one of: MUTABLE or IMMUTABLE. Defaults to IMMUTABLE | `string` | `"MUTABLE"` | no |
| <a name="input_spid_validator"></a> [spid\_validator](#input\_spid\_validator) | Spid validator configurations. When null the resources won't be created. | <pre>object({<br>    cluster_arn  = string<br>    service_name = string<br>    cpu          = optional(number, 512)<br>    memory       = optional(number, 1024)<br>    container = object({<br>      name                = string<br>      image_name          = string<br>      image_version       = string<br>      cpu                 = optional(number, 512)<br>      memory              = optional(number, 1024)<br>      logs_retention_days = optional(number, 14)<br>      environment = list(object({<br>        name  = string<br>        value = string<br>      }))<br>    })<br>  })</pre> | `null` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_ecr_endpoint"></a> [ecr\_endpoint](#output\_ecr\_endpoint) | n/a |
| <a name="output_route53_record_fqdn"></a> [route53\_record\_fqdn](#output\_route53\_record\_fqdn) | n/a |
