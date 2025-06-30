<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | >=5.49 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | >=5.49 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_cognito_presignup_lambda"></a> [cognito\_presignup\_lambda](#module\_cognito\_presignup\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |

## Resources

| Name | Type |
|------|------|
| [aws_cognito_user_pool.main](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user_pool) | resource |
| [aws_cognito_user_pool_client.client](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user_pool_client) | resource |
| [aws_cognito_user_pool_domain.auth](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user_pool_domain) | resource |
| [aws_route53_record.auth_admin](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_cognito"></a> [cognito](#input\_cognito) | n/a | <pre>object({<br/>    user_pool_name       = string,<br/>    user_pool_domain     = string,<br/>    user_pool_client     = string,<br/>    logout_url           = string, #https://dev.oneid.pagopa.it/logout<br/>    callback_url         = string,<br/>    auth_certificate_arn = string,<br/>    acm_domain_name      = string<br/>  })</pre> | n/a | yes |
| <a name="input_cognito_presignup_lambda"></a> [cognito\_presignup\_lambda](#input\_cognito\_presignup\_lambda) | n/a | <pre>object({<br/>    name                              = string<br/>    filename                          = string<br/>    cloudwatch_logs_retention_in_days = number<br/>  })</pre> | n/a | yes |
| <a name="input_r53_dns_zone_id"></a> [r53\_dns\_zone\_id](#input\_r53\_dns\_zone\_id) | n/a | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_cloudfront_distribution"></a> [cloudfront\_distribution](#output\_cloudfront\_distribution) | n/a |
| <a name="output_cloudfront_distribution_zone_id"></a> [cloudfront\_distribution\_zone\_id](#output\_cloudfront\_distribution\_zone\_id) | n/a |
| <a name="output_user_pool_arn"></a> [user\_pool\_arn](#output\_user\_pool\_arn) | n/a |
| <a name="output_user_pool_id"></a> [user\_pool\_id](#output\_user\_pool\_id) | n/a |
<!-- END_TF_DOCS -->