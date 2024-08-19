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
| [aws_api_gateway_account.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_account) | resource |
| [aws_api_gateway_api_key.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_api_key) | resource |
| [aws_api_gateway_deployment.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_deployment) | resource |
| [aws_api_gateway_domain_name.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_domain_name) | resource |
| [aws_api_gateway_method_settings.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_method_settings) | resource |
| [aws_api_gateway_rest_api.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_rest_api) | resource |
| [aws_api_gateway_stage.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_stage) | resource |
| [aws_api_gateway_usage_plan.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_usage_plan) | resource |
| [aws_api_gateway_usage_plan_key.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_usage_plan_key) | resource |
| [aws_apigatewayv2_api_mapping.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/apigatewayv2_api_mapping) | resource |
| [aws_cloudwatch_log_group.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_log_group) | resource |
| [aws_iam_role.apigw](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role_policy.cloudwatch](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_body"></a> [body](#input\_body) | Open api json body | `string` | n/a | yes |
| <a name="input_endpoint_configuration"></a> [endpoint\_configuration](#input\_endpoint\_configuration) | n/a | <pre>object({<br>    types            = list(string)<br>    vpc_endpoint_ids = optional(list(string), null)<br>  })</pre> | n/a | yes |
| <a name="input_name"></a> [name](#input\_name) | Rest api name | `string` | n/a | yes |
| <a name="input_plan"></a> [plan](#input\_plan) | Name of the plan associated to the set of apis. | <pre>object({<br>    name                 = string<br>    throttle_burst_limit = number<br>    throttle_rate_limit  = number<br>    api_key_name         = optional(string, null)<br>  })</pre> | n/a | yes |
| <a name="input_stage_name"></a> [stage\_name](#input\_stage\_name) | Stage name. | `string` | n/a | yes |
| <a name="input_api_cache_cluster_enabled"></a> [api\_cache\_cluster\_enabled](#input\_api\_cache\_cluster\_enabled) | Enablr cache cluster is enabled for the stage. | `bool` | `false` | no |
| <a name="input_api_cache_cluster_size"></a> [api\_cache\_cluster\_size](#input\_api\_cache\_cluster\_size) | Size of the cache cluster for the stage, if enabled. | `number` | `0.5` | no |
| <a name="input_api_mapping_key"></a> [api\_mapping\_key](#input\_api\_mapping\_key) | The API mapping key. | `string` | `null` | no |
| <a name="input_certificate_arn"></a> [certificate\_arn](#input\_certificate\_arn) | Api Gateway certificate arn | `string` | `null` | no |
| <a name="input_create_custom_domain_name"></a> [create\_custom\_domain\_name](#input\_create\_custom\_domain\_name) | Create custom domain name. If true the custom\_domain\_name can not be null. | `bool` | `false` | no |
| <a name="input_custom_domain_name"></a> [custom\_domain\_name](#input\_custom\_domain\_name) | Api gateway custom domain name. | `string` | `null` | no |
| <a name="input_method_settings"></a> [method\_settings](#input\_method\_settings) | List of Api Gateway method settings. | <pre>list(object({<br>    method_path                             = string<br>    metrics_enabled                         = optional(bool, false)<br>    logging_level                           = optional(string, "OFF")<br>    data_trace_enabled                      = optional(bool, false)<br>    throttling_rate_limit                   = optional(number, -1)<br>    throttling_burst_limit                  = optional(number, -1)<br>    caching_enabled                         = optional(bool, false)<br>    cache_ttl_in_seconds                    = optional(number, 0)<br>    cache_data_encrypted                    = optional(bool, false)<br>    require_authorization_for_cache_control = optional(bool, false)<br>    cache_key_parameters                    = optional(list(string), [])<br>  }))</pre> | `[]` | no |
| <a name="input_xray_tracing_enabled"></a> [xray\_tracing\_enabled](#input\_xray\_tracing\_enabled) | Whether active tracing with X-ray is enabled. | `bool` | `false` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_domain_name"></a> [domain\_name](#output\_domain\_name) | n/a |
| <a name="output_regional_domain_name"></a> [regional\_domain\_name](#output\_regional\_domain\_name) | n/a |
| <a name="output_regional_zone_id"></a> [regional\_zone\_id](#output\_regional\_zone\_id) | n/a |
| <a name="output_rest_api_execution_arn"></a> [rest\_api\_execution\_arn](#output\_rest\_api\_execution\_arn) | n/a |
| <a name="output_rest_api_id"></a> [rest\_api\_id](#output\_rest\_api\_id) | n/a |
| <a name="output_rest_api_invoke_url"></a> [rest\_api\_invoke\_url](#output\_rest\_api\_invoke\_url) | n/a |
| <a name="output_rest_api_name"></a> [rest\_api\_name](#output\_rest\_api\_name) | n/a |

<!-- BEGIN_TF_DOCS -->
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
| [aws_api_gateway_account.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_account) | resource |
| [aws_api_gateway_api_key.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_api_key) | resource |
| [aws_api_gateway_deployment.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_deployment) | resource |
| [aws_api_gateway_domain_name.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_domain_name) | resource |
| [aws_api_gateway_method_settings.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_method_settings) | resource |
| [aws_api_gateway_rest_api.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_rest_api) | resource |
| [aws_api_gateway_stage.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_stage) | resource |
| [aws_api_gateway_usage_plan.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_usage_plan) | resource |
| [aws_api_gateway_usage_plan_key.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_usage_plan_key) | resource |
| [aws_apigatewayv2_api_mapping.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/apigatewayv2_api_mapping) | resource |
| [aws_cloudwatch_log_group.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_log_group) | resource |
| [aws_iam_role.apigw](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role_policy.cloudwatch](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_api_cache_cluster_enabled"></a> [api\_cache\_cluster\_enabled](#input\_api\_cache\_cluster\_enabled) | Enablr cache cluster is enabled for the stage. | `bool` | `false` | no |
| <a name="input_api_cache_cluster_size"></a> [api\_cache\_cluster\_size](#input\_api\_cache\_cluster\_size) | Size of the cache cluster for the stage, if enabled. | `number` | `0.5` | no |
| <a name="input_api_mapping_key"></a> [api\_mapping\_key](#input\_api\_mapping\_key) | The API mapping key. | `string` | `null` | no |
| <a name="input_body"></a> [body](#input\_body) | Open api json body | `string` | n/a | yes |
| <a name="input_certificate_arn"></a> [certificate\_arn](#input\_certificate\_arn) | Api Gateway certificate arn | `string` | `null` | no |
| <a name="input_create_custom_domain_name"></a> [create\_custom\_domain\_name](#input\_create\_custom\_domain\_name) | Create custom domain name. If true the custom\_domain\_name can not be null. | `bool` | `false` | no |
| <a name="input_custom_domain_name"></a> [custom\_domain\_name](#input\_custom\_domain\_name) | Api gateway custom domain name. | `string` | `null` | no |
| <a name="input_endpoint_configuration"></a> [endpoint\_configuration](#input\_endpoint\_configuration) | n/a | <pre>object({<br>    types            = list(string)<br>    vpc_endpoint_ids = optional(list(string), null)<br>  })</pre> | n/a | yes |
| <a name="input_method_settings"></a> [method\_settings](#input\_method\_settings) | List of Api Gateway method settings. | <pre>list(object({<br>    method_path                             = string<br>    metrics_enabled                         = optional(bool, false)<br>    logging_level                           = optional(string, "OFF")<br>    data_trace_enabled                      = optional(bool, false)<br>    throttling_rate_limit                   = optional(number, -1)<br>    throttling_burst_limit                  = optional(number, -1)<br>    caching_enabled                         = optional(bool, false)<br>    cache_ttl_in_seconds                    = optional(number, 0)<br>    cache_data_encrypted                    = optional(bool, false)<br>    require_authorization_for_cache_control = optional(bool, false)<br>    cache_key_parameters                    = optional(list(string), [])<br>  }))</pre> | `[]` | no |
| <a name="input_name"></a> [name](#input\_name) | Rest api name | `string` | n/a | yes |
| <a name="input_plan"></a> [plan](#input\_plan) | Name of the plan associated to the set of apis. | <pre>object({<br>    name                 = string<br>    throttle_burst_limit = number<br>    throttle_rate_limit  = number<br>    api_key_name         = optional(string, null)<br>  })</pre> | n/a | yes |
| <a name="input_stage_name"></a> [stage\_name](#input\_stage\_name) | Stage name. | `string` | n/a | yes |
| <a name="input_xray_tracing_enabled"></a> [xray\_tracing\_enabled](#input\_xray\_tracing\_enabled) | Whether active tracing with X-ray is enabled. | `bool` | `false` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_domain_name"></a> [domain\_name](#output\_domain\_name) | n/a |
| <a name="output_regional_domain_name"></a> [regional\_domain\_name](#output\_regional\_domain\_name) | n/a |
| <a name="output_regional_zone_id"></a> [regional\_zone\_id](#output\_regional\_zone\_id) | n/a |
| <a name="output_rest_api_execution_arn"></a> [rest\_api\_execution\_arn](#output\_rest\_api\_execution\_arn) | n/a |
| <a name="output_rest_api_id"></a> [rest\_api\_id](#output\_rest\_api\_id) | n/a |
| <a name="output_rest_api_invoke_url"></a> [rest\_api\_invoke\_url](#output\_rest\_api\_invoke\_url) | n/a |
| <a name="output_rest_api_name"></a> [rest\_api\_name](#output\_rest\_api\_name) | n/a |
<!-- END_TF_DOCS -->