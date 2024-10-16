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

No modules.

## Resources

| Name | Type |
|------|------|
| [aws_cloudwatch_dashboard.api_methods](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_dashboard) | resource |
| [aws_cloudwatch_dashboard.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_dashboard) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_api_methods_dashboard_name"></a> [api\_methods\_dashboard\_name](#input\_api\_methods\_dashboard\_name) | Name of the api methods dashboard. | `string` | n/a | yes |
| <a name="input_api_name"></a> [api\_name](#input\_api\_name) | n/a | `string` | n/a | yes |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | n/a | `string` | n/a | yes |
| <a name="input_dynamodb_table_name"></a> [dynamodb\_table\_name](#input\_dynamodb\_table\_name) | n/a | `string` | n/a | yes |
| <a name="input_ecs"></a> [ecs](#input\_ecs) | n/a | <pre>object({<br>    service_name = string,<br>    cluster_name = string<br>  })</pre> | n/a | yes |
| <a name="input_main_dashboard_name"></a> [main\_dashboard\_name](#input\_main\_dashboard\_name) | Name of the main dashboard. | `string` | n/a | yes |
| <a name="input_nlb"></a> [nlb](#input\_nlb) | Network load balancer configurations. | <pre>object({<br>    arn_suffix              = string<br>    target_group_arn_suffix = string<br>  })</pre> | n/a | yes |

## Outputs

No outputs.

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.49 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 5.49 |

## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [aws_cloudwatch_dashboard.api_methods](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/cloudwatch_dashboard) | resource |
| [aws_cloudwatch_dashboard.main](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/cloudwatch_dashboard) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_api_methods_dashboard_name"></a> [api\_methods\_dashboard\_name](#input\_api\_methods\_dashboard\_name) | Name of the api methods dashboard. | `string` | n/a | yes |
| <a name="input_api_name"></a> [api\_name](#input\_api\_name) | n/a | `string` | n/a | yes |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | n/a | `string` | n/a | yes |
| <a name="input_client_registrations_table"></a> [client\_registrations\_table](#input\_client\_registrations\_table) | Dynamodb ClientRegistrations table | `string` | n/a | yes |
| <a name="input_ecs"></a> [ecs](#input\_ecs) | n/a | <pre>object({<br>    service_name = string,<br>    cluster_name = string<br>  })</pre> | n/a | yes |
| <a name="input_main_dashboard_name"></a> [main\_dashboard\_name](#input\_main\_dashboard\_name) | Name of the main dashboard. | `string` | n/a | yes |
| <a name="input_nlb"></a> [nlb](#input\_nlb) | Network load balancer configurations. | <pre>object({<br>    arn_suffix              = string<br>    target_group_arn_suffix = string<br>  })</pre> | n/a | yes |
| <a name="input_sessions_table"></a> [sessions\_table](#input\_sessions\_table) | Dynamodb Sessions table | `string` | n/a | yes |

## Outputs

No outputs.
<!-- END_TF_DOCS -->