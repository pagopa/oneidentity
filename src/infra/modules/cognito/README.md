<!-- BEGIN_TF_DOCS -->
## Requirements

No requirements.

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | n/a |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_cognito_presignup_lambda"></a> [cognito\_presignup\_lambda](#module\_cognito\_presignup\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |

## Resources

| Name | Type |
|------|------|
| [aws_cognito_user_pool.main](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user_pool) | resource |
| [aws_cognito_user_pool_client.client](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user_pool_client) | resource |
| [aws_cognito_user_pool_domain.main](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cognito_user_pool_domain) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_cognito"></a> [cognito](#input\_cognito) | n/a | <pre>object({<br>    user_pool_name   = string,<br>    user_pool_domain = string,<br>    user_pool_client = string,<br>    logout_url       = string, #https://dev.oneid.pagopa.it/logout<br>    callback_url     = string<br>  })</pre> | n/a | yes |
| <a name="input_cognito_presignup_lambda"></a> [cognito\_presignup\_lambda](#input\_cognito\_presignup\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    cloudwatch_logs_retention_in_days = number<br>  })</pre> | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_user_pool_arn"></a> [user\_pool\_arn](#output\_user\_pool\_arn) | n/a |
<!-- END_TF_DOCS -->