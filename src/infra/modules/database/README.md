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
| <a name="module_dynamodb_sessions_table"></a> [dynamodb\_sessions\_table](#module\_dynamodb\_sessions\_table) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_dynamodb_table_client_registrations"></a> [dynamodb\_table\_client\_registrations](#module\_dynamodb\_table\_client\_registrations) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_kms_sessions_table"></a> [kms\_sessions\_table](#module\_kms\_sessions\_table) | terraform-aws-modules/kms/aws | 2.2.1 |

## Resources

| Name | Type |
|------|------|
| [aws_cloudwatch_log_group.pipe_logs](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_log_group) | resource |
| [aws_iam_role.pipe_sessions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role_policy.pipe_source](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy) | resource |
| [aws_pipes_pipe.dynamodb_to_cloudwatch](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/pipes_pipe) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_client_registrations_table"></a> [client\_registrations\_table](#input\_client\_registrations\_table) | Client configurations. | <pre>object({<br>    point_in_time_recovery_enabled = optional(bool, false)<br>  })</pre> | n/a | yes |
| <a name="input_sessions_table"></a> [sessions\_table](#input\_sessions\_table) | Saml responses table configurations. | <pre>object({<br>    ttl_enabled                    = optional(bool, true)<br>    point_in_time_recovery_enabled = optional(bool, false)<br>    stream_enabled                 = optional(bool, false)<br>    stream_view_type               = string<br>  })</pre> | n/a | yes |
| <a name="input_account_id"></a> [account\_id](#input\_account\_id) | AWS account id | `string` | `null` | no |
| <a name="input_eventbridge_pipe_sessions"></a> [eventbridge\_pipe\_sessions](#input\_eventbridge\_pipe\_sessions) | n/a | <pre>object({<br>    pipe_name = string<br><br>  })</pre> | `null` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_kms_sessions_table_alias_arn"></a> [kms\_sessions\_table\_alias\_arn](#output\_kms\_sessions\_table\_alias\_arn) | n/a |
| <a name="output_table_client_registrations_arn"></a> [table\_client\_registrations\_arn](#output\_table\_client\_registrations\_arn) | n/a |
| <a name="output_table_client_registrations_name"></a> [table\_client\_registrations\_name](#output\_table\_client\_registrations\_name) | n/a |
| <a name="output_table_sessions_arn"></a> [table\_sessions\_arn](#output\_table\_sessions\_arn) | n/a |
| <a name="output_table_sessions_gsi_code_arn"></a> [table\_sessions\_gsi\_code\_arn](#output\_table\_sessions\_gsi\_code\_arn) | n/a |
| <a name="output_table_sessions_name"></a> [table\_sessions\_name](#output\_table\_sessions\_name) | n/a |
