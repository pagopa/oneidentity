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
| <a name="module_acm"></a> [acm](#module\_acm) | terraform-aws-modules/acm/aws | 5.0.0 |
| <a name="module_records"></a> [records](#module\_records) | terraform-aws-modules/route53/aws//modules/records | 2.11.0 |
| <a name="module_rest_api"></a> [rest\_api](#module\_rest\_api) | ../rest-api | n/a |
| <a name="module_zones"></a> [zones](#module\_zones) | terraform-aws-modules/route53/aws//modules/zones | 2.11.0 |

## Resources

| Name | Type |
|------|------|
| [aws_api_gateway_vpc_link.apigw](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_vpc_link) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_api_gateway_plan"></a> [api\_gateway\_plan](#input\_api\_gateway\_plan) | Name of the plan associated to the set of apis. | <pre>object({<br>    name                 = string<br>    throttle_burst_limit = number<br>    throttle_rate_limit  = number<br>  })</pre> | n/a | yes |
| <a name="input_api_gateway_target_arns"></a> [api\_gateway\_target\_arns](#input\_api\_gateway\_target\_arns) | List of target arn for the api gateway. | `list(string)` | n/a | yes |
| <a name="input_nlb_dns_name"></a> [nlb\_dns\_name](#input\_nlb\_dns\_name) | NLB dns name. | `string` | n/a | yes |
| <a name="input_public_subnet_ids"></a> [public\_subnet\_ids](#input\_public\_subnet\_ids) | Public subnet ids. | `list(string)` | n/a | yes |
| <a name="input_r53_dns_zones"></a> [r53\_dns\_zones](#input\_r53\_dns\_zones) | R53 DNS Zones. | `any` | n/a | yes |
| <a name="input_rest_api_name"></a> [rest\_api\_name](#input\_rest\_api\_name) | Rest api name | `string` | n/a | yes |
| <a name="input_vpc_id"></a> [vpc\_id](#input\_vpc\_id) | VPC id. | `string` | n/a | yes |
| <a name="input_rest_api_stage"></a> [rest\_api\_stage](#input\_rest\_api\_stage) | Rest api stage name | `string` | `"v1"` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_acm_certificate_arn"></a> [acm\_certificate\_arn](#output\_acm\_certificate\_arn) | n/a |
| <a name="output_acm_validation_domains"></a> [acm\_validation\_domains](#output\_acm\_validation\_domains) | # ACM ## |
| <a name="output_rest_api_invoke_url"></a> [rest\_api\_invoke\_url](#output\_rest\_api\_invoke\_url) | n/a |
| <a name="output_route53_zone_name_servers"></a> [route53\_zone\_name\_servers](#output\_route53\_zone\_name\_servers) | # DNS ## |
