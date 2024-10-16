<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.49 |

## Providers

No providers.

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_r53_zones"></a> [r53\_zones](#module\_r53\_zones) | terraform-aws-modules/route53/aws//modules/zones | 2.11.0 |

## Resources

No resources.

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_r53_dns_zones"></a> [r53\_dns\_zones](#input\_r53\_dns\_zones) | R53 DNS Zones. | `any` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_dns_zone_arn"></a> [dns\_zone\_arn](#output\_dns\_zone\_arn) | n/a |
| <a name="output_dns_zone_id"></a> [dns\_zone\_id](#output\_dns\_zone\_id) | n/a |
| <a name="output_dns_zone_name"></a> [dns\_zone\_name](#output\_dns\_zone\_name) | n/a |
| <a name="output_dns_zone_name_servers"></a> [dns\_zone\_name\_servers](#output\_dns\_zone\_name\_servers) | n/a |
<!-- END_TF_DOCS -->