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
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | >=5.49 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | >=5.49 |
| <a name="provider_aws.us_east_1"></a> [aws.us\_east\_1](#provider\_aws.us\_east\_1) | >=5.49 |
| <a name="provider_random"></a> [random](#provider\_random) | n/a |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_acm"></a> [acm](#module\_acm) | terraform-aws-modules/acm/aws | 5.0.0 |
| <a name="module_acm_admin"></a> [acm\_admin](#module\_acm\_admin) | terraform-aws-modules/acm/aws | 5.0.0 |
| <a name="module_acm_internal_idp"></a> [acm\_internal\_idp](#module\_acm\_internal\_idp) | terraform-aws-modules/acm/aws | 5.0.0 |
| <a name="module_records"></a> [records](#module\_records) | terraform-aws-modules/route53/aws//modules/records | 2.11.0 |
| <a name="module_rest_api"></a> [rest\_api](#module\_rest\_api) | ../rest-api | n/a |
| <a name="module_rest_api_admin"></a> [rest\_api\_admin](#module\_rest\_api\_admin) | ../rest-api | n/a |
| <a name="module_rest_api_internal_idp"></a> [rest\_api\_internal\_idp](#module\_rest\_api\_internal\_idp) | ../rest-api | n/a |
| <a name="module_webacl_count_alarm"></a> [webacl\_count\_alarm](#module\_webacl\_count\_alarm) | terraform-aws-modules/cloudwatch/aws//modules/metric-alarms-by-multiple-dimensions | 5.6.0 |

## Resources

| Name | Type |
|------|------|
| [aws_acm_certificate.auth](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate) | resource |
| [aws_acm_certificate_validation.auth](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate_validation) | resource |
| [aws_api_gateway_vpc_link.apigw](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/api_gateway_vpc_link) | resource |
| [aws_cloudwatch_metric_alarm.api_alarms](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_iam_policy.lambda_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_policy.s3_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_role.lambda_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role.s3_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role_policy_attachment.lambda_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.s3_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_route53_record.certificate](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record) | resource |
| [aws_s3_object.openapi_exp](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_object) | resource |
| [aws_wafv2_web_acl.main](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/wafv2_web_acl) | resource |
| [aws_wafv2_web_acl_association.main](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/wafv2_web_acl_association) | resource |
| [random_id.suffix](https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/id) | resource |
| [aws_api_gateway_export.api_exp](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/api_gateway_export) | data source |
| [aws_iam_policy_document.apigw_assume_role](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.lambda_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.s3_apigw_proxy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_api_alarms"></a> [api\_alarms](#input\_api\_alarms) | n/a | <pre>map(object({<br/>    metric_name         = string<br/>    namespace           = string<br/>    threshold           = number<br/>    evaluation_periods  = number<br/>    period              = number<br/>    statistic           = string<br/>    comparison_operator = string<br/>    resource_name       = string<br/>    sns_topic_alarm_arn = string<br/>    method              = string<br/>  }))</pre> | n/a | yes |
| <a name="input_api_authorizer_admin_name"></a> [api\_authorizer\_admin\_name](#input\_api\_authorizer\_admin\_name) | n/a | `string` | `null` | no |
| <a name="input_api_authorizer_name"></a> [api\_authorizer\_name](#input\_api\_authorizer\_name) | n/a | `string` | `null` | no |
| <a name="input_api_cache_cluster_enabled"></a> [api\_cache\_cluster\_enabled](#input\_api\_cache\_cluster\_enabled) | Enablr cache cluster is enabled for the stage. | `bool` | `false` | no |
| <a name="input_api_cache_cluster_size"></a> [api\_cache\_cluster\_size](#input\_api\_cache\_cluster\_size) | Size of the cache cluster for the stage, if enabled. | `number` | `0.5` | no |
| <a name="input_api_gateway_admin_plan"></a> [api\_gateway\_admin\_plan](#input\_api\_gateway\_admin\_plan) | Name of the plan associated to the set of apis. | <pre>object({<br/>    name                 = string<br/>    throttle_burst_limit = number<br/>    throttle_rate_limit  = number<br/>  })</pre> | n/a | yes |
| <a name="input_api_gateway_internal_idp_plan"></a> [api\_gateway\_internal\_idp\_plan](#input\_api\_gateway\_internal\_idp\_plan) | Name of the plan associated to the set of apis. | <pre>object({<br/>    name                 = string<br/>    throttle_burst_limit = number<br/>    throttle_rate_limit  = number<br/>  })</pre> | <pre>{<br/>  "name": "internal-idp-plan",<br/>  "throttle_burst_limit": 1000,<br/>  "throttle_rate_limit": 1000<br/>}</pre> | no |
| <a name="input_api_gateway_plan"></a> [api\_gateway\_plan](#input\_api\_gateway\_plan) | Name of the plan associated to the set of apis. | <pre>object({<br/>    name                 = string<br/>    throttle_burst_limit = number<br/>    throttle_rate_limit  = number<br/>    api_key_name         = optional(string, null)<br/>  })</pre> | n/a | yes |
| <a name="input_api_gateway_target_arns"></a> [api\_gateway\_target\_arns](#input\_api\_gateway\_target\_arns) | List of target arn for the api gateway. | `list(string)` | n/a | yes |
| <a name="input_api_method_settings"></a> [api\_method\_settings](#input\_api\_method\_settings) | List of Api Gateway method settings. | <pre>list(object({<br/>    method_path                             = string<br/>    metrics_enabled                         = optional(bool, false)<br/>    logging_level                           = optional(string, "OFF")<br/>    data_trace_enabled                      = optional(bool, false)<br/>    throttling_rate_limit                   = optional(number, -1)<br/>    throttling_burst_limit                  = optional(number, -1)<br/>    caching_enabled                         = optional(bool, false)<br/>    cache_ttl_in_seconds                    = optional(number, 0)<br/>    cache_data_encrypted                    = optional(bool, false)<br/>    require_authorization_for_cache_control = optional(bool, false)<br/>    cache_key_parameters                    = optional(list(string), [])<br/>  }))</pre> | `[]` | no |
| <a name="input_assets_bucket_arn"></a> [assets\_bucket\_arn](#input\_assets\_bucket\_arn) | Assets bucket arn. | `string` | n/a | yes |
| <a name="input_assets_bucket_name"></a> [assets\_bucket\_name](#input\_assets\_bucket\_name) | Assets bucket name. | `string` | n/a | yes |
| <a name="input_assets_control_panel_bucket_arn"></a> [assets\_control\_panel\_bucket\_arn](#input\_assets\_control\_panel\_bucket\_arn) | Assets bucket arn. | `string` | n/a | yes |
| <a name="input_assets_control_panel_bucket_name"></a> [assets\_control\_panel\_bucket\_name](#input\_assets\_control\_panel\_bucket\_name) | Assets bucket control panel name. | `string` | n/a | yes |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS Region. | `string` | n/a | yes |
| <a name="input_client_manager_lambda_arn"></a> [client\_manager\_lambda\_arn](#input\_client\_manager\_lambda\_arn) | lambda client manager arn | `string` | n/a | yes |
| <a name="input_client_registration_lambda_arn"></a> [client\_registration\_lambda\_arn](#input\_client\_registration\_lambda\_arn) | lambda client registration arn | `string` | n/a | yes |
| <a name="input_cognito_domain_cloudfront_distribution"></a> [cognito\_domain\_cloudfront\_distribution](#input\_cognito\_domain\_cloudfront\_distribution) | n/a | `string` | `null` | no |
| <a name="input_cognito_domain_cloudfront_distribution_zone_id"></a> [cognito\_domain\_cloudfront\_distribution\_zone\_id](#input\_cognito\_domain\_cloudfront\_distribution\_zone\_id) | n/a | `string` | `null` | no |
| <a name="input_cors_allow_origins"></a> [cors\_allow\_origins](#input\_cors\_allow\_origins) | List of allowed origins for CORS. | `string` | `null` | no |
| <a name="input_create_custom_domain_admin_name"></a> [create\_custom\_domain\_admin\_name](#input\_create\_custom\_domain\_admin\_name) | ApiGw create custom domain admin name. | `bool` | `true` | no |
| <a name="input_create_custom_domain_name"></a> [create\_custom\_domain\_name](#input\_create\_custom\_domain\_name) | ApiGw create custom domain admin name. | `bool` | `true` | no |
| <a name="input_create_dns_record"></a> [create\_dns\_record](#input\_create\_dns\_record) | Create DNS record to associate the API Gateway RestApi to the hosted zone. | `bool` | `true` | no |
| <a name="input_deploy_internal_idp_rest_api"></a> [deploy\_internal\_idp\_rest\_api](#input\_deploy\_internal\_idp\_rest\_api) | ApiGW deploy internal idp api. | `bool` | `false` | no |
| <a name="input_dns_record_ttl"></a> [dns\_record\_ttl](#input\_dns\_record\_ttl) | DNS records ttl | `number` | n/a | yes |
| <a name="input_domain_admin_name"></a> [domain\_admin\_name](#input\_domain\_admin\_name) | DNS domain name. | `string` | n/a | yes |
| <a name="input_domain_auth_name"></a> [domain\_auth\_name](#input\_domain\_auth\_name) | DNS domain name. | `string` | `null` | no |
| <a name="input_domain_internal_idp_name"></a> [domain\_internal\_idp\_name](#input\_domain\_internal\_idp\_name) | DNS domain name. | `string` | `null` | no |
| <a name="input_domain_name"></a> [domain\_name](#input\_domain\_name) | DNS domain name. | `string` | n/a | yes |
| <a name="input_nlb_dns_name"></a> [nlb\_dns\_name](#input\_nlb\_dns\_name) | NLB dns name. | `string` | n/a | yes |
| <a name="input_openapi_admin_template_file"></a> [openapi\_admin\_template\_file](#input\_openapi\_admin\_template\_file) | Openapi admin template file path. | `string` | n/a | yes |
| <a name="input_openapi_internal_idp_template_file"></a> [openapi\_internal\_idp\_template\_file](#input\_openapi\_internal\_idp\_template\_file) | Openapi internal idp template file path. | `string` | `null` | no |
| <a name="input_openapi_template_file"></a> [openapi\_template\_file](#input\_openapi\_template\_file) | Openapi template file path. | `string` | n/a | yes |
| <a name="input_provider_arn"></a> [provider\_arn](#input\_provider\_arn) | Value of the provider arn. | `string` | `""` | no |
| <a name="input_r53_dns_zone_id"></a> [r53\_dns\_zone\_id](#input\_r53\_dns\_zone\_id) | R53 dns zone id. | `string` | n/a | yes |
| <a name="input_rest_api_admin_name"></a> [rest\_api\_admin\_name](#input\_rest\_api\_admin\_name) | Rest api admin name | `string` | n/a | yes |
| <a name="input_rest_api_admin_stage"></a> [rest\_api\_admin\_stage](#input\_rest\_api\_admin\_stage) | Rest api admin stage name | `string` | `"v1"` | no |
| <a name="input_rest_api_internal_idp_name"></a> [rest\_api\_internal\_idp\_name](#input\_rest\_api\_internal\_idp\_name) | Rest api internal idp name | `string` | `null` | no |
| <a name="input_rest_api_internal_idp_stage"></a> [rest\_api\_internal\_idp\_stage](#input\_rest\_api\_internal\_idp\_stage) | Rest api internal idp stage name | `string` | `"v1"` | no |
| <a name="input_rest_api_name"></a> [rest\_api\_name](#input\_rest\_api\_name) | Rest api name | `string` | n/a | yes |
| <a name="input_rest_api_stage"></a> [rest\_api\_stage](#input\_rest\_api\_stage) | Rest api stage name | `string` | `"v1"` | no |
| <a name="input_retrieve_status_lambda_arn"></a> [retrieve\_status\_lambda\_arn](#input\_retrieve\_status\_lambda\_arn) | lambda retrieve status arn | `string` | n/a | yes |
| <a name="input_role_prefix"></a> [role\_prefix](#input\_role\_prefix) | Prefix to assign to the IAM Roles | `string` | n/a | yes |
| <a name="input_user_pool_arn"></a> [user\_pool\_arn](#input\_user\_pool\_arn) | n/a | `string` | `""` | no |
| <a name="input_web_acl"></a> [web\_acl](#input\_web\_acl) | WEB acl name | <pre>object({<br/>    name                       = string<br/>    cloudwatch_metrics_enabled = optional(bool, false)<br/>    sampled_requests_enabled   = optional(bool, false)<br/>    sns_topic_arn              = optional(string, "")<br/>  })</pre> | n/a | yes |
| <a name="input_xray_tracing_enabled"></a> [xray\_tracing\_enabled](#input\_xray\_tracing\_enabled) | Whether active tracing with X-ray is enabled. | `bool` | `false` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_acm_auth_certificate_arn"></a> [acm\_auth\_certificate\_arn](#output\_acm\_auth\_certificate\_arn) | n/a |
| <a name="output_acm_certificate_arn"></a> [acm\_certificate\_arn](#output\_acm\_certificate\_arn) | n/a |
| <a name="output_acm_domain_name"></a> [acm\_domain\_name](#output\_acm\_domain\_name) | n/a |
| <a name="output_acm_validation_domains"></a> [acm\_validation\_domains](#output\_acm\_validation\_domains) | # ACM ## |
| <a name="output_api_name"></a> [api\_name](#output\_api\_name) | n/a |
| <a name="output_rest_api_arn"></a> [rest\_api\_arn](#output\_rest\_api\_arn) | n/a |
| <a name="output_rest_api_execution_arn"></a> [rest\_api\_execution\_arn](#output\_rest\_api\_execution\_arn) | n/a |
| <a name="output_rest_api_id"></a> [rest\_api\_id](#output\_rest\_api\_id) | n/a |
| <a name="output_rest_api_invoke_url"></a> [rest\_api\_invoke\_url](#output\_rest\_api\_invoke\_url) | n/a |
| <a name="output_rest_api_stage_name"></a> [rest\_api\_stage\_name](#output\_rest\_api\_stage\_name) | n/a |
<!-- END_TF_DOCS -->