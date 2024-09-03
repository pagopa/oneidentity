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
| <a name="module_dev_ns_record"></a> [dev\_ns\_record](#module\_dev\_ns\_record) | terraform-aws-modules/route53/aws//modules/records | 2.11.0 |
| <a name="module_r53_zones"></a> [r53\_zones](#module\_r53\_zones) | terraform-aws-modules/route53/aws//modules/zones | 2.11.0 |

## Resources

| Name | Type |
|------|------|
| [aws_caller_identity.current](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/data-sources/caller_identity) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_r53_dns_zone"></a> [r53\_dns\_zone](#input\_r53\_dns\_zone) | # R53 DNS zone ## | <pre>object({<br>    name    = string<br>    comment = string<br>  })</pre> | n/a | yes |
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | App name. | `string` | `"oneid"` | no |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS region to create resources. Default Milan | `string` | `"eu-south-1"` | no |
| <a name="input_aws_region_short"></a> [aws\_region\_short](#input\_aws\_region\_short) | AWS region short format. | `string` | `"es-1"` | no |
| <a name="input_dns_record_ttl"></a> [dns\_record\_ttl](#input\_dns\_record\_ttl) | Dns record ttl (in sec) | `number` | `86400` | no |
| <a name="input_env_short"></a> [env\_short](#input\_env\_short) | Evnironment short. | `string` | `"d"` | no |
| <a name="input_tags"></a> [tags](#input\_tags) | n/a | `map(any)` | <pre>{<br>  "CreatedBy": "Terraform"<br>}</pre> | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_zone_ns_servers"></a> [zone\_ns\_servers](#output\_zone\_ns\_servers) | n/a |

<!-- BEGIN_TF_DOCS -->
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
| <a name="module_dev_ns_record"></a> [dev\_ns\_record](#module\_dev\_ns\_record) | terraform-aws-modules/route53/aws//modules/records | 2.11.0 |
| <a name="module_r53_zones"></a> [r53\_zones](#module\_r53\_zones) | ../../modules/dns | n/a |

## Resources

| Name | Type |
|------|------|
| [aws_caller_identity.current](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/data-sources/caller_identity) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | App name. | `string` | `"oneid"` | no |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS region to create resources. Default Milan | `string` | `"eu-south-1"` | no |
| <a name="input_aws_region_short"></a> [aws\_region\_short](#input\_aws\_region\_short) | AWS region short format. | `string` | `"es-1"` | no |
| <a name="input_dns_record_ttl"></a> [dns\_record\_ttl](#input\_dns\_record\_ttl) | Dns record ttl (in sec) | `number` | `86400` | no |
| <a name="input_env_short"></a> [env\_short](#input\_env\_short) | Evnironment short. | `string` | `"d"` | no |
| <a name="input_r53_dns_zone"></a> [r53\_dns\_zone](#input\_r53\_dns\_zone) | # R53 DNS zone ## | <pre>object({<br>    name    = string<br>    comment = string<br>  })</pre> | <pre>{<br>  "comment": "Oneidentity prod zone.",<br>  "name": "oneid.pagopa.it"<br>}</pre> | no |
| <a name="input_tags"></a> [tags](#input\_tags) | n/a | `map(any)` | <pre>{<br>  "CostCenter": "tier0",<br>  "CreatedBy": "Terraform",<br>  "Environment": "Prod",<br>  "Owner": "Oneidentity",<br>  "Source": "https://github.com/pagopa/oneidentity"<br>}</pre> | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_zone_ns_servers"></a> [zone\_ns\_servers](#output\_zone\_ns\_servers) | n/a |
<!-- END_TF_DOCS -->