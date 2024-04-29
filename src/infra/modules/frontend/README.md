## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38 |

## Providers

No providers.

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_acm"></a> [acm](#module\_acm) | terraform-aws-modules/acm/aws | 5.0.0 |
| <a name="module_alb"></a> [alb](#module\_alb) | terraform-aws-modules/alb/aws | 9.7.0 |
| <a name="module_records"></a> [records](#module\_records) | terraform-aws-modules/route53/aws//modules/records | 2.11.0 |
| <a name="module_zones"></a> [zones](#module\_zones) | terraform-aws-modules/route53/aws//modules/zones | 2.11.0 |

## Resources

No resources.

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_acm_certificate_arn"></a> [acm\_certificate\_arn](#input\_acm\_certificate\_arn) | Acm certificate arn. | `string` | n/a | yes |
| <a name="input_alb_name"></a> [alb\_name](#input\_alb\_name) | Load balancer name. | `string` | n/a | yes |
| <a name="input_public_subnet_ids"></a> [public\_subnet\_ids](#input\_public\_subnet\_ids) | Public subnet ids. | `list(string)` | n/a | yes |
| <a name="input_r53_dns_zones"></a> [r53\_dns\_zones](#input\_r53\_dns\_zones) | R53 DNS Zones. | `any` | n/a | yes |
| <a name="input_vpc_id"></a> [vpc\_id](#input\_vpc\_id) | VPC id. | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_acm_certificate_arn"></a> [acm\_certificate\_arn](#output\_acm\_certificate\_arn) | n/a |
| <a name="output_acm_validation_domains"></a> [acm\_validation\_domains](#output\_acm\_validation\_domains) | # ACM ## |
| <a name="output_alb_dns_name"></a> [alb\_dns\_name](#output\_alb\_dns\_name) | n/a |
| <a name="output_alb_security_group_id"></a> [alb\_security\_group\_id](#output\_alb\_security\_group\_id) | n/a |
| <a name="output_alb_target_groups"></a> [alb\_target\_groups](#output\_alb\_target\_groups) | n/a |
| <a name="output_route53_zone_name_servers"></a> [route53\_zone\_name\_servers](#output\_route53\_zone\_name\_servers) | # DNS ## |
