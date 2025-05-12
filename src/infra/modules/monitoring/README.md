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
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | >=5.49 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | >=5.49 |

## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [aws_ce_anomaly_monitor.service_monitor](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ce_anomaly_monitor) | resource |
| [aws_ce_anomaly_subscription.main](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ce_anomaly_subscription) | resource |
| [aws_cloudwatch_dashboard.api_methods](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_dashboard) | resource |
| [aws_cloudwatch_dashboard.detailed_metrics_dashboard](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_dashboard) | resource |
| [aws_cloudwatch_dashboard.main](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_dashboard) | resource |
| [aws_cloudwatch_query_definition.client_registration_log_level_error](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_query_definition) | resource |
| [aws_cloudwatch_query_definition.ecs_log_level_error](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_query_definition) | resource |
| [aws_cloudwatch_query_definition.metadata_log_level_error](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_query_definition) | resource |
| [aws_ssm_parameter.alarm_subscribers](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/ssm_parameter) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_alarm_subscribers"></a> [alarm\_subscribers](#input\_alarm\_subscribers) | SSM parameter store with the list alarm subscribers. | `string` | n/a | yes |
| <a name="input_api_methods_dashboard_name"></a> [api\_methods\_dashboard\_name](#input\_api\_methods\_dashboard\_name) | Name of the api methods dashboard. | `string` | n/a | yes |
| <a name="input_api_name"></a> [api\_name](#input\_api\_name) | n/a | `string` | n/a | yes |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | n/a | `string` | n/a | yes |
| <a name="input_ce_daily_budget"></a> [ce\_daily\_budget](#input\_ce\_daily\_budget) | Cost Explorer daily budget. | `string` | `"300"` | no |
| <a name="input_client_registrations_table"></a> [client\_registrations\_table](#input\_client\_registrations\_table) | Dynamodb ClientRegistrations table | `string` | n/a | yes |
| <a name="input_clients"></a> [clients](#input\_clients) | n/a | <pre>list(object({<br/>    client_id     = string<br/>    friendly_name = string<br/>  }))</pre> | `[]` | no |
| <a name="input_create_ce_budget"></a> [create\_ce\_budget](#input\_create\_ce\_budget) | Create Cost Explorer budget. | `bool` | `false` | no |
| <a name="input_detailed_metrics_dashboard_name"></a> [detailed\_metrics\_dashboard\_name](#input\_detailed\_metrics\_dashboard\_name) | Name of the detailed metrics dashboard. | `string` | n/a | yes |
| <a name="input_ecs"></a> [ecs](#input\_ecs) | n/a | <pre>object({<br/>    service_name   = string,<br/>    cluster_name   = string<br/>    log_group_name = string<br/>  })</pre> | n/a | yes |
| <a name="input_env_short"></a> [env\_short](#input\_env\_short) | Name of the main dashboard. | `string` | n/a | yes |
| <a name="input_idp_entity_ids"></a> [idp\_entity\_ids](#input\_idp\_entity\_ids) | n/a | `list(string)` | `[]` | no |
| <a name="input_lambda_client_registration"></a> [lambda\_client\_registration](#input\_lambda\_client\_registration) | n/a | <pre>object({<br/>    log_group_name = string<br/>  })</pre> | n/a | yes |
| <a name="input_lambda_metadata"></a> [lambda\_metadata](#input\_lambda\_metadata) | n/a | <pre>object({<br/>    log_group_name = string<br/>  })</pre> | n/a | yes |
| <a name="input_main_dashboard_name"></a> [main\_dashboard\_name](#input\_main\_dashboard\_name) | Name of the main dashboard. | `string` | n/a | yes |
| <a name="input_nlb"></a> [nlb](#input\_nlb) | Network load balancer configurations. | <pre>object({<br/>    arn_suffix              = string<br/>    target_group_arn_suffix = string<br/>  })</pre> | n/a | yes |
| <a name="input_query_files"></a> [query\_files](#input\_query\_files) | n/a | `list(string)` | `[]` | no |
| <a name="input_region_short"></a> [region\_short](#input\_region\_short) | AWS Region short format. | `string` | n/a | yes |
| <a name="input_sessions_table"></a> [sessions\_table](#input\_sessions\_table) | Dynamodb Sessions table | `string` | n/a | yes |

## Outputs

No outputs.
<!-- END_TF_DOCS -->