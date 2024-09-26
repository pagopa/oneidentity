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
| [aws_lambda_permission.allow_api_gw_invoke_metadata](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/lambda_permission) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_api_gateway_plan"></a> [api\_gateway\_plan](#input\_api\_gateway\_plan) | Name of the plan associated to the set of apis. | <pre>object({<br>    name                 = string<br>    throttle_burst_limit = number<br>    throttle_rate_limit  = number<br>    api_key_name         = optional(string, null)<br>  })</pre> | n/a | yes |
| <a name="input_api_gateway_target_arns"></a> [api\_gateway\_target\_arns](#input\_api\_gateway\_target\_arns) | List of target arn for the api gateway. | `list(string)` | n/a | yes |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS Region. | `string` | n/a | yes |
| <a name="input_client_registration_lambda_arn"></a> [client\_registration\_lambda\_arn](#input\_client\_registration\_lambda\_arn) | lambda client registration arn | `string` | n/a | yes |
| <a name="input_dns_record_ttl"></a> [dns\_record\_ttl](#input\_dns\_record\_ttl) | DNS records ttl | `number` | n/a | yes |
| <a name="input_metadata_lamba_arn"></a> [metadata\_lamba\_arn](#input\_metadata\_lamba\_arn) | lambda metadata arn | `string` | n/a | yes |
| <a name="input_metadata_lamba_name"></a> [metadata\_lamba\_name](#input\_metadata\_lamba\_name) | Lambda metadata name | `string` | n/a | yes |
| <a name="input_nlb_dns_name"></a> [nlb\_dns\_name](#input\_nlb\_dns\_name) | NLB dns name. | `string` | n/a | yes |
| <a name="input_r53_dns_zones"></a> [r53\_dns\_zones](#input\_r53\_dns\_zones) | R53 DNS Zones. | `any` | n/a | yes |
| <a name="input_rest_api_name"></a> [rest\_api\_name](#input\_rest\_api\_name) | Rest api name | `string` | n/a | yes |
| <a name="input_api_cache_cluster_enabled"></a> [api\_cache\_cluster\_enabled](#input\_api\_cache\_cluster\_enabled) | Enablr cache cluster is enabled for the stage. | `bool` | `false` | no |
| <a name="input_api_cache_cluster_size"></a> [api\_cache\_cluster\_size](#input\_api\_cache\_cluster\_size) | Size of the cache cluster for the stage, if enabled. | `number` | `0.5` | no |
| <a name="input_api_method_settings"></a> [api\_method\_settings](#input\_api\_method\_settings) | List of Api Gateway method settings. | <pre>list(object({<br>    method_path                             = string<br>    metrics_enabled                         = optional(bool, false)<br>    logging_level                           = optional(string, "OFF")<br>    data_trace_enabled                      = optional(bool, false)<br>    throttling_rate_limit                   = optional(number, -1)<br>    throttling_burst_limit                  = optional(number, -1)<br>    caching_enabled                         = optional(bool, false)<br>    cache_ttl_in_seconds                    = optional(number, 0)<br>    cache_data_encrypted                    = optional(bool, false)<br>    require_authorization_for_cache_control = optional(bool, false)<br>    cache_key_parameters                    = optional(list(string), [])<br>  }))</pre> | `[]` | no |
| <a name="input_rest_api_stage"></a> [rest\_api\_stage](#input\_rest\_api\_stage) | Rest api stage name | `string` | `"v1"` | no |
| <a name="input_xray_tracing_enabled"></a> [xray\_tracing\_enabled](#input\_xray\_tracing\_enabled) | Whether active tracing with X-ray is enabled. | `bool` | `false` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_acm_certificate_arn"></a> [acm\_certificate\_arn](#output\_acm\_certificate\_arn) | n/a |
| <a name="output_acm_validation_domains"></a> [acm\_validation\_domains](#output\_acm\_validation\_domains) | # ACM ## |
| <a name="output_api_name"></a> [api\_name](#output\_api\_name) | n/a |
| <a name="output_rest_api_invoke_url"></a> [rest\_api\_invoke\_url](#output\_rest\_api\_invoke\_url) | n/a |
| <a name="output_route53_zone_id"></a> [route53\_zone\_id](#output\_route53\_zone\_id) | n/a |
| <a name="output_route53_zone_name_servers"></a> [route53\_zone\_name\_servers](#output\_route53\_zone\_name\_servers) | # DNS ## |
| <a name="output_zone_name"></a> [zone\_name](#output\_zone\_name) | n/a |

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
| <a name="provider_random"></a> [random](#provider\_random) | n/a |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_acm"></a> [acm](#module\_acm) | terraform-aws-modules/acm/aws | 5.0.0 |
| <a name="module_records"></a> [records](#module\_records) | terraform-aws-modules/route53/aws//modules/records | 2.11.0 |
| <a name="module_rest_api"></a> [rest\_api](#module\_rest\_api) | ../rest-api | n/a |
| <a name="module_webacl_count_alarm"></a> [webacl\_count\_alarm](#module\_webacl\_count\_alarm) | git::https://github.com/terraform-aws-modules/terraform-aws-cloudwatch.git//modules/metric-alarms-by-multiple-dimensions | 60cf981e0f1ae033699e5b274440867e48289967 |

## Resources

| Name | Type |
|------|------|
| [aws_api_gateway_vpc_link.apigw](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/api_gateway_vpc_link) | resource |
| [aws_cloudwatch_metric_alarm.api_alarms](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_iam_policy.lambda_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_policy.s3_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_role.lambda_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role.s3_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role_policy_attachment.lambda_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.s3_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy_attachment) | resource |
| [aws_lambda_permission.allow_api_gw_invoke_metadata](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/lambda_permission) | resource |
| [aws_wafv2_web_acl.main](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/wafv2_web_acl) | resource |
| [random_id.suffix](https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/id) | resource |
| [aws_iam_policy_document.apigw_assume_role](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.lambda_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.s3_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_api_alarms"></a> [api\_alarms](#input\_api\_alarms) | n/a | <pre>map(object({<br>    metric_name         = string<br>    namespace           = string<br>    threshold           = number<br>    evaluation_periods  = number<br>    period              = number<br>    statistic           = string<br>    comparison_operator = string<br>    resource_name       = string<br>    sns_topic_alarm_arn = string<br>    method              = string<br>  }))</pre> | n/a | yes |
| <a name="input_api_cache_cluster_enabled"></a> [api\_cache\_cluster\_enabled](#input\_api\_cache\_cluster\_enabled) | Enablr cache cluster is enabled for the stage. | `bool` | `false` | no |
| <a name="input_api_cache_cluster_size"></a> [api\_cache\_cluster\_size](#input\_api\_cache\_cluster\_size) | Size of the cache cluster for the stage, if enabled. | `number` | `0.5` | no |
| <a name="input_api_gateway_plan"></a> [api\_gateway\_plan](#input\_api\_gateway\_plan) | Name of the plan associated to the set of apis. | <pre>object({<br>    name                 = string<br>    throttle_burst_limit = number<br>    throttle_rate_limit  = number<br>    api_key_name         = optional(string, null)<br>  })</pre> | n/a | yes |
| <a name="input_api_gateway_target_arns"></a> [api\_gateway\_target\_arns](#input\_api\_gateway\_target\_arns) | List of target arn for the api gateway. | `list(string)` | n/a | yes |
| <a name="input_api_method_settings"></a> [api\_method\_settings](#input\_api\_method\_settings) | List of Api Gateway method settings. | <pre>list(object({<br>    method_path                             = string<br>    metrics_enabled                         = optional(bool, false)<br>    logging_level                           = optional(string, "OFF")<br>    data_trace_enabled                      = optional(bool, false)<br>    throttling_rate_limit                   = optional(number, -1)<br>    throttling_burst_limit                  = optional(number, -1)<br>    caching_enabled                         = optional(bool, false)<br>    cache_ttl_in_seconds                    = optional(number, 0)<br>    cache_data_encrypted                    = optional(bool, false)<br>    require_authorization_for_cache_control = optional(bool, false)<br>    cache_key_parameters                    = optional(list(string), [])<br>  }))</pre> | `[]` | no |
| <a name="input_assets_bucket_arn"></a> [assets\_bucket\_arn](#input\_assets\_bucket\_arn) | Assets bucket arn. | `string` | n/a | yes |
| <a name="input_assets_bucket_name"></a> [assets\_bucket\_name](#input\_assets\_bucket\_name) | Assets bucket name. | `string` | n/a | yes |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS Region. | `string` | n/a | yes |
| <a name="input_client_registration_lambda_arn"></a> [client\_registration\_lambda\_arn](#input\_client\_registration\_lambda\_arn) | lambda client registration arn | `string` | n/a | yes |
| <a name="input_dns_record_ttl"></a> [dns\_record\_ttl](#input\_dns\_record\_ttl) | DNS records ttl | `number` | n/a | yes |
| <a name="input_domain_name"></a> [domain\_name](#input\_domain\_name) | DNS domain name. | `string` | n/a | yes |
| <a name="input_metadata_lamba_arn"></a> [metadata\_lamba\_arn](#input\_metadata\_lamba\_arn) | lambda metadata arn | `string` | n/a | yes |
| <a name="input_metadata_lamba_name"></a> [metadata\_lamba\_name](#input\_metadata\_lamba\_name) | Lambda metadata name | `string` | n/a | yes |
| <a name="input_nlb_dns_name"></a> [nlb\_dns\_name](#input\_nlb\_dns\_name) | NLB dns name. | `string` | n/a | yes |
| <a name="input_openapi_template_file"></a> [openapi\_template\_file](#input\_openapi\_template\_file) | Openapi template file path. | `string` | n/a | yes |
| <a name="input_r53_dns_zone_id"></a> [r53\_dns\_zone\_id](#input\_r53\_dns\_zone\_id) | R53 dns zone id. | `string` | n/a | yes |
| <a name="input_rest_api_name"></a> [rest\_api\_name](#input\_rest\_api\_name) | Rest api name | `string` | n/a | yes |
| <a name="input_rest_api_stage"></a> [rest\_api\_stage](#input\_rest\_api\_stage) | Rest api stage name | `string` | `"v1"` | no |
| <a name="input_web_acl"></a> [web\_acl](#input\_web\_acl) | WEB acl name | <pre>object({<br>    name                       = string<br>    cloudwatch_metrics_enabled = optional(bool,false)<br>    sampled_requests_enabled   = optional(bool,false)<br>    sns_topic_arn              = optional(string,"")<br>  })</pre> | n/a | yes |
| <a name="input_xray_tracing_enabled"></a> [xray\_tracing\_enabled](#input\_xray\_tracing\_enabled) | Whether active tracing with X-ray is enabled. | `bool` | `false` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_acm_certificate_arn"></a> [acm\_certificate\_arn](#output\_acm\_certificate\_arn) | n/a |
| <a name="output_acm_validation_domains"></a> [acm\_validation\_domains](#output\_acm\_validation\_domains) | # ACM ## |
| <a name="output_api_name"></a> [api\_name](#output\_api\_name) | n/a |
| <a name="output_rest_api_invoke_url"></a> [rest\_api\_invoke\_url](#output\_rest\_api\_invoke\_url) | n/a |
<!-- END_TF_DOCS -->