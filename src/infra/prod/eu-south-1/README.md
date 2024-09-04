## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | 1.7.4 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38.0 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 5.38.0 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_dev_ns_record"></a> [dev\_ns\_record](#module\_dev\_ns\_record) | terraform-aws-modules/route53/aws//modules/records | 2.11.0 |
| <a name="module_r53_zones"></a> [r53\_zones](#module\_r53\_zones) | terraform-aws-modules/route53/aws//modules/zones | 2.11.0 |

## Resources

| Name | Type |
|------|------|
| [aws_caller_identity.current](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/data-sources/caller_identity) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_r53_dns_zone"></a> [r53\_dns\_zone](#input\_r53\_dns\_zone) | # R53 DNS zone ## | <pre>object({<br>    name    = string<br>    comment = string<br>  })</pre> | n/a | yes |
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | App name. | `string` | `"oneid"` | no |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS region to create resources. Default Milan | `string` | `"eu-south-1"` | no |
| <a name="input_aws_region_short"></a> [aws\_region\_short](#input\_aws\_region\_short) | AWS region short format. | `string` | `"es-1"` | no |
| <a name="input_dns_record_ttl"></a> [dns\_record\_ttl](#input\_dns\_record\_ttl) | Dns record ttl (in sec) | `number` | `86400` | no |
| <a name="input_env_short"></a> [env\_short](#input\_env\_short) | Evnironment short. | `string` | `"d"` | no |
| <a name="input_tags"></a> [tags](#input\_tags) | n/a | `map(any)` | <pre>{<br>  "CreatedBy": "Terraform"<br>}</pre> | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_zone_ns_servers"></a> [zone\_ns\_servers](#output\_zone\_ns\_servers) | n/a |

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | 1.7.4 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38.0 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 5.38.0 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_dev_ns_record"></a> [dev\_ns\_record](#module\_dev\_ns\_record) | terraform-aws-modules/route53/aws//modules/records | 2.11.0 |
| <a name="module_iam"></a> [iam](#module\_iam) | ../../modules/iam | n/a |
| <a name="module_r53_zones"></a> [r53\_zones](#module\_r53\_zones) | ../../modules/dns | n/a |

## Resources

| Name | Type |
|------|------|
| [aws_caller_identity.current](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/data-sources/caller_identity) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_api_cache_cluster_enabled"></a> [api\_cache\_cluster\_enabled](#input\_api\_cache\_cluster\_enabled) | Enable cache cluster is enabled for the stage. | `bool` | `true` | no |
| <a name="input_api_cache_cluster_size"></a> [api\_cache\_cluster\_size](#input\_api\_cache\_cluster\_size) | Size of the cache cluster for the stage, if enabled. | `number` | `0.5` | no |
| <a name="input_api_method_settings"></a> [api\_method\_settings](#input\_api\_method\_settings) | List of Api Gateway method settings. | <pre>list(object({<br>    method_path                             = string<br>    metrics_enabled                         = optional(bool, false)<br>    logging_level                           = optional(string, "OFF")<br>    data_trace_enabled                      = optional(bool, false)<br>    throttling_rate_limit                   = optional(number, -1)<br>    throttling_burst_limit                  = optional(number, -1)<br>    caching_enabled                         = optional(bool, false)<br>    cache_ttl_in_seconds                    = optional(number, 0)<br>    cache_data_encrypted                    = optional(bool, false)<br>    require_authorization_for_cache_control = optional(bool, false)<br>    cache_key_parameters                    = optional(list(string), [])<br>  }))</pre> | <pre>[<br>  {<br>    "logging_level": "INFO",<br>    "method_path": "*/*",<br>    "metrics_enabled": true<br>  },<br>  {<br>    "cache_ttl_in_seconds": 3600,<br>    "caching_enabled": true,<br>    "method_path": "saml/{id_type}/metadata/GET",<br>    "metrics_enabled": true<br>  },<br>  {<br>    "cache_ttl_in_seconds": 3600,<br>    "caching_enabled": true,<br>    "method_path": "static/{proxy+}/GET"<br>  },<br>  {<br>    "cache_ttl_in_seconds": 3600,<br>    "caching_enabled": true,<br>    "method_path": "login/GET"<br>  },<br>  {<br>    "cache_ttl_in_seconds": 3600,<br>    "caching_enabled": true,<br>    "method_path": "login/error/GET"<br>  }<br>]</pre> | no |
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | App name. | `string` | `"oneid"` | no |
| <a name="input_assertion_bucket"></a> [assertion\_bucket](#input\_assertion\_bucket) | Assertion storage configurations. | <pre>object({<br>    mfa_delete               = bool<br>    glacier_transaction_days = number<br>    expiration_days          = number<br>  })</pre> | <pre>{<br>  "expiration_days": 1080,<br>  "glacier_transaction_days": 90,<br>  "mfa_delete": false<br>}</pre> | no |
| <a name="input_assertions_crawler_schedule"></a> [assertions\_crawler\_schedule](#input\_assertions\_crawler\_schedule) | A cron expression used to specify the schedule | `string` | `"cron(00 08 ? * MON *)"` | no |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS region to create resources. Default Milan | `string` | `"eu-south-1"` | no |
| <a name="input_aws_region_short"></a> [aws\_region\_short](#input\_aws\_region\_short) | AWS region short format. | `string` | `"es-1"` | no |
| <a name="input_client_registrations_table"></a> [client\_registrations\_table](#input\_client\_registrations\_table) | Client configurations table. | <pre>object({<br>    point_in_time_recovery_enabled = optional(bool, false)<br>  })</pre> | <pre>{<br>  "point_in_time_recovery_enabled": true<br>}</pre> | no |
| <a name="input_dlq_alarms"></a> [dlq\_alarms](#input\_dlq\_alarms) | n/a | <pre>object({<br>    metric_name         = string<br>    namespace           = string<br>    threshold           = optional(number)<br>    evaluation_periods  = optional(number)<br>    period              = optional(number)<br>    statistic           = optional(string)<br>    comparison_operator = optional(string)<br>    sns_topic_alarm_arn = optional(list(string))<br>  })</pre> | <pre>{<br>  "comparison_operator": "GreaterThanOrEqualToThreshold",<br>  "evaluation_periods": 1,<br>  "metric_name": "ApproximateNumberOfMessagesVisible",<br>  "namespace": "AWS/SQS",<br>  "period": 300,<br>  "statistic": "Sum",<br>  "threshold": 0<br>}</pre> | no |
| <a name="input_dlq_assertion_setting"></a> [dlq\_assertion\_setting](#input\_dlq\_assertion\_setting) | n/a | <pre>object({<br>    maximum_retry_attempts        = number<br>    maximum_record_age_in_seconds = number<br>  })</pre> | <pre>{<br>  "maximum_record_age_in_seconds": 604800,<br>  "maximum_retry_attempts": 3<br>}</pre> | no |
| <a name="input_dns_record_ttl"></a> [dns\_record\_ttl](#input\_dns\_record\_ttl) | Dns record ttl (in sec) | `number` | `3600` | no |
| <a name="input_ecs_alarms"></a> [ecs\_alarms](#input\_ecs\_alarms) | n/a | <pre>map(object({<br>    metric_name         = string<br>    namespace           = string<br>    threshold           = optional(number)<br>    evaluation_periods  = optional(number)<br>    period              = optional(number)<br>    statistic           = optional(string)<br>    comparison_operator = optional(string)<br>  }))</pre> | <pre>{<br>  "ecs-cpu-utilization": {<br>    "comparison_operator": "GreaterThanOrEqualToThreshold",<br>    "evaluation_periods": 1,<br>    "metric_name": "CPUUtilization",<br>    "namespace": "AWS/ECS",<br>    "period": 300,<br>    "statistic": "Average"<br>  },<br>  "ecs-memory-utilization": {<br>    "comparison_operator": "GreaterThanOrEqualToThreshold",<br>    "evaluation_periods": 1,<br>    "metric_name": "MemoryUtilization",<br>    "namespace": "AWS/ECS",<br>    "period": 300,<br>    "statistic": "Average"<br>  }<br>}</pre> | no |
| <a name="input_ecs_enable_container_insights"></a> [ecs\_enable\_container\_insights](#input\_ecs\_enable\_container\_insights) | Enable ecs cluster container inight. | `bool` | `true` | no |
| <a name="input_ecs_oneid_core"></a> [ecs\_oneid\_core](#input\_ecs\_oneid\_core) | Oneidentity core backend configurations. | <pre>object({<br>    image_version    = string<br>    cpu              = number<br>    memory           = number<br>    container_cpu    = number<br>    container_memory = number<br>    autoscaling = object({<br>      enable        = bool<br>      desired_count = number<br>      min_capacity  = number<br>      max_capacity  = number<br>    })<br>    logs_retention_days   = number<br>    app_spid_test_enabled = optional(bool, false)<br>  })</pre> | <pre>{<br>  "autoscaling": {<br>    "desired_count": 3,<br>    "enable": true,<br>    "max_capacity": 12,<br>    "min_capacity": 1<br>  },<br>  "container_cpu": 512,<br>  "container_memory": 1024,<br>  "cpu": 512,<br>  "image_version": "83b0593b0f113eee056786850de51ecfe5079789",<br>  "logs_retention_days": 30,<br>  "memory": 1024<br>}</pre> | no |
| <a name="input_enable_nat_gateway"></a> [enable\_nat\_gateway](#input\_enable\_nat\_gateway) | Create nat gateway(s) | `bool` | `false` | no |
| <a name="input_env_short"></a> [env\_short](#input\_env\_short) | Evnironment short. | `string` | `"p"` | no |
| <a name="input_lambda_alarms"></a> [lambda\_alarms](#input\_lambda\_alarms) | n/a | <pre>object({<br>    metric_name         = string<br>    namespace           = string<br>    threshold           = optional(number)<br>    evaluation_periods  = optional(number)<br>    period              = optional(number)<br>    statistic           = optional(string)<br>    comparison_operator = optional(string)<br>    sns_topic_alarm_arn = optional(list(string))<br>  })</pre> | <pre>{<br>  "comparison_operator": "GreaterThanOrEqualToThreshold",<br>  "evaluation_periods": 1,<br>  "metric_name": "Errors",<br>  "namespace": "AWS/Lambda",<br>  "period": 300,<br>  "statistic": "Sum",<br>  "threshold": 1<br>}</pre> | no |
| <a name="input_lambda_cloudwatch_logs_retention_in_days"></a> [lambda\_cloudwatch\_logs\_retention\_in\_days](#input\_lambda\_cloudwatch\_logs\_retention\_in\_days) | Cloudwatch log group retention days. | `number` | `30` | no |
| <a name="input_number_of_images_to_keep"></a> [number\_of\_images\_to\_keep](#input\_number\_of\_images\_to\_keep) | Number of images to keeps in ECR. | `number` | `10` | no |
| <a name="input_r53_dns_zone"></a> [r53\_dns\_zone](#input\_r53\_dns\_zone) | # R53 DNS zone ## | <pre>object({<br>    name    = string<br>    comment = string<br>  })</pre> | <pre>{<br>  "comment": "Oneidentity prod hosted zone.",<br>  "name": "oneid.pagopa.it"<br>}</pre> | no |
| <a name="input_repository_image_tag_mutability"></a> [repository\_image\_tag\_mutability](#input\_repository\_image\_tag\_mutability) | The tag mutability setting for the repository. Must be one of: MUTABLE or IMMUTABLE. Defaults to IMMUTABLE | `string` | `"MUTABLE"` | no |
| <a name="input_rest_api_throttle_settings"></a> [rest\_api\_throttle\_settings](#input\_rest\_api\_throttle\_settings) | Rest api throttle settings. | <pre>object({<br>    burst_limit = number<br>    rate_limit  = number<br>  })</pre> | <pre>{<br>  "burst_limit": 500,<br>  "rate_limit": 300<br>}</pre> | no |
| <a name="input_sessions_table"></a> [sessions\_table](#input\_sessions\_table) | Saml responses table configurations. | <pre>object({<br>    ttl_enabled                    = bool<br>    point_in_time_recovery_enabled = bool<br>    stream_enabled                 = bool<br>    stream_view_type               = string<br>  })</pre> | <pre>{<br>  "point_in_time_recovery_enabled": false,<br>  "stream_enabled": true,<br>  "stream_view_type": "NEW_IMAGE",<br>  "ttl_enabled": true<br>}</pre> | no |
| <a name="input_single_nat_gateway"></a> [single\_nat\_gateway](#input\_single\_nat\_gateway) | Create a single nat gateway to spare money. | `bool` | `false` | no |
| <a name="input_tags"></a> [tags](#input\_tags) | n/a | `map(any)` | <pre>{<br>  "CostCenter": "tier0",<br>  "CreatedBy": "Terraform",<br>  "Environment": "Prod",<br>  "Owner": "Oneidentity",<br>  "Source": "https://github.com/pagopa/oneidentity"<br>}</pre> | no |
| <a name="input_vpc_cidr"></a> [vpc\_cidr](#input\_vpc\_cidr) | VPC address space | `string` | `"10.0.0.0/17"` | no |
| <a name="input_vpc_internal_subnets_cidr"></a> [vpc\_internal\_subnets\_cidr](#input\_vpc\_internal\_subnets\_cidr) | Internal subnets address spaces. | `list(string)` | <pre>[<br>  "10.0.32.0/20",<br>  "10.0.16.0/20",<br>  "10.0.0.0/20"<br>]</pre> | no |
| <a name="input_vpc_private_subnets_cidr"></a> [vpc\_private\_subnets\_cidr](#input\_vpc\_private\_subnets\_cidr) | Private subnets address spaces. | `list(string)` | <pre>[<br>  "10.0.80.0/20",<br>  "10.0.64.0/20",<br>  "10.0.48.0/20"<br>]</pre> | no |
| <a name="input_vpc_public_subnets_cidr"></a> [vpc\_public\_subnets\_cidr](#input\_vpc\_public\_subnets\_cidr) | Public subnets address spaces. | `list(string)` | <pre>[<br>  "10.0.120.0/21",<br>  "10.0.112.0/21",<br>  "10.0.104.0/21"<br>]</pre> | no |
| <a name="input_xray_tracing_enabled"></a> [xray\_tracing\_enabled](#input\_xray\_tracing\_enabled) | Whether active tracing with X-ray is enabled. | `bool` | `true` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_zone_ns_servers"></a> [zone\_ns\_servers](#output\_zone\_ns\_servers) | n/a |
<!-- END_TF_DOCS -->