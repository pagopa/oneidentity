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
| <a name="module_assertion_lambda"></a> [assertion\_lambda](#module\_assertion\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_client_registration_lambda"></a> [client\_registration\_lambda](#module\_client\_registration\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_ecr"></a> [ecr](#module\_ecr) | terraform-aws-modules/ecr/aws | 1.6.0 |
| <a name="module_ecs_cluster"></a> [ecs\_cluster](#module\_ecs\_cluster) | terraform-aws-modules/ecs/aws | 5.9.1 |
| <a name="module_ecs_core_service"></a> [ecs\_core\_service](#module\_ecs\_core\_service) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |
| <a name="module_elb"></a> [elb](#module\_elb) | terraform-aws-modules/alb/aws | 9.8.0 |
| <a name="module_idp_metadata_lambda"></a> [idp\_metadata\_lambda](#module\_idp\_metadata\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_is_gh_integration_lambda"></a> [is\_gh\_integration\_lambda](#module\_is\_gh\_integration\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_jwt_sign"></a> [jwt\_sign](#module\_jwt\_sign) | terraform-aws-modules/kms/aws | 2.2.1 |
| <a name="module_metadata_lambda"></a> [metadata\_lambda](#module\_metadata\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_security_group_lambda_assertion"></a> [security\_group\_lambda\_assertion](#module\_security\_group\_lambda\_assertion) | terraform-aws-modules/security-group/aws | 4.17.2 |
| <a name="module_security_group_lambda_client_registration"></a> [security\_group\_lambda\_client\_registration](#module\_security\_group\_lambda\_client\_registration) | terraform-aws-modules/security-group/aws | 4.17.2 |
| <a name="module_security_group_lambda_idp_metadata"></a> [security\_group\_lambda\_idp\_metadata](#module\_security\_group\_lambda\_idp\_metadata) | terraform-aws-modules/security-group/aws | 4.17.2 |
| <a name="module_security_group_lambda_metadata"></a> [security\_group\_lambda\_metadata](#module\_security\_group\_lambda\_metadata) | terraform-aws-modules/security-group/aws | 4.17.2 |

## Resources

| Name | Type |
|------|------|
| [aws_cloudwatch_log_group.ecs_core](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_log_group) | resource |
| [aws_cloudwatch_metric_alarm.dlq_assertions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_cloudwatch_metric_alarm.dlq_sessions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_cloudwatch_metric_alarm.ecs_alarms](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_cloudwatch_metric_alarm.lambda_errors](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_iam_policy.deploy_ecs](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_policy.deploy_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_policy.ecs_core_task](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_role.github_lambda_deploy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role.githubecsdeploy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role.pipe_sessions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role_policy.pipe_source](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy) | resource |
| [aws_iam_role_policy_attachment.deploy_ecs](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.deploy_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy_attachment) | resource |
| [aws_pipes_pipe.sessions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/pipes_pipe) | resource |
| [aws_s3_bucket_notification.bucket_notification](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/s3_bucket_notification) | resource |
| [aws_security_group_rule.metadata_vpc_tls](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/security_group_rule) | resource |
| [aws_sns_topic_subscription.is-gh-integration](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/sns_topic_subscription) | resource |
| [aws_sqs_queue.dlq_lambda_assertion](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/sqs_queue) | resource |
| [aws_sqs_queue.pipe_dlq](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/sqs_queue) | resource |
| [aws_iam_policy_document.assertion_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.client_registration_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.idp_metadata_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.is_gh_integration_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.metadata_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |
| [aws_ssm_parameter.certificate](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/ssm_parameter) | data source |
| [aws_ssm_parameter.is_gh_integration_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/ssm_parameter) | data source |
| [aws_ssm_parameter.key](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/ssm_parameter) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_account_id"></a> [account\_id](#input\_account\_id) | AWS Account id. | `string` | n/a | yes |
| <a name="input_assertion_lambda"></a> [assertion\_lambda](#input\_assertion\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    s3_assertion_bucket_arn           = string<br>    kms_assertion_key_arn             = string<br>    environment_variables             = map(string)<br>    cloudwatch_logs_retention_in_days = number<br>    vpc_s3_prefix_id                  = string<br>    vpc_subnet_ids                    = list(string)<br>    vpc_id                            = string<br>  })</pre> | n/a | yes |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS Region. | `string` | n/a | yes |
| <a name="input_client_registration_lambda"></a> [client\_registration\_lambda](#input\_client\_registration\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    table_client_registrations_arn    = string<br>    cloudwatch_logs_retention_in_days = number<br>    vpc_id                            = string<br>    vpc_endpoint_dynamodb_prefix_id   = string<br>    vpc_subnet_ids                    = list(string)<br><br>  })</pre> | n/a | yes |
| <a name="input_dlq_alarms"></a> [dlq\_alarms](#input\_dlq\_alarms) | n/a | <pre>object({<br>    metric_name         = string<br>    namespace           = string<br>    threshold           = number<br>    evaluation_periods  = number<br>    period              = number<br>    statistic           = string<br>    comparison_operator = string<br>    sns_topic_alarm_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_dynamodb_table_idpMetadata"></a> [dynamodb\_table\_idpMetadata](#input\_dynamodb\_table\_idpMetadata) | Dynamodb table idpMetadata anrs | <pre>object({<br>    table_arn       = string<br>    gsi_pointer_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_dynamodb_table_sessions"></a> [dynamodb\_table\_sessions](#input\_dynamodb\_table\_sessions) | Dynamodb table sessions anrs | <pre>object({<br>    table_arn    = string<br>    gsi_code_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_ecr_registers"></a> [ecr\_registers](#input\_ecr\_registers) | ECR image repositories | <pre>list(object({<br>    name                            = string<br>    number_of_images_to_keep        = number<br>    repository_image_tag_mutability = optional(string, "IMMUTABLE")<br>  }))</pre> | n/a | yes |
| <a name="input_ecs_alarms"></a> [ecs\_alarms](#input\_ecs\_alarms) | n/a | <pre>map(object({<br>    metric_name         = string<br>    namespace           = string<br>    threshold           = number<br>    evaluation_periods  = number<br>    period              = number<br>    statistic           = string<br>    comparison_operator = string<br>    sns_topic_alarm_arn = string<br>  }))</pre> | n/a | yes |
| <a name="input_ecs_cluster_name"></a> [ecs\_cluster\_name](#input\_ecs\_cluster\_name) | ECS Cluster name | `string` | n/a | yes |
| <a name="input_fargate_capacity_providers"></a> [fargate\_capacity\_providers](#input\_fargate\_capacity\_providers) | n/a | <pre>map(object({<br>    default_capacity_provider_strategy = object({<br>      weight = number<br>      base   = number<br>    })<br>  }))</pre> | n/a | yes |
| <a name="input_github_repository"></a> [github\_repository](#input\_github\_repository) | Github repository responsible to deploy ECS tasks in the form <organization\|user/repository>. | `string` | n/a | yes |
| <a name="input_idp_metadata_lambda"></a> [idp\_metadata\_lambda](#input\_idp\_metadata\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    environment_variables             = map(string)<br>    s3_idp_metadata_bucket_arn        = string<br>    s3_idp_metadata_bucket_id         = string<br>    vpc_id                            = string<br>    vpc_subnet_ids                    = list(string)<br>    vpc_s3_prefix_id                  = string<br>    cloudwatch_logs_retention_in_days = number<br>  })</pre> | n/a | yes |
| <a name="input_is_gh_integration_lambda"></a> [is\_gh\_integration\_lambda](#input\_is\_gh\_integration\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    sns_topic_arn                     = optional(string, null)<br>    cloudwatch_logs_retention_in_days = string<br>    ssm_parameter_name                = optional(string, "GH_PERSONAL_ACCESS_TOKEN")<br>  })</pre> | n/a | yes |
| <a name="input_kms_sessions_table_alias_arn"></a> [kms\_sessions\_table\_alias\_arn](#input\_kms\_sessions\_table\_alias\_arn) | Kms key used to encrypt and dectypt session table. | `string` | n/a | yes |
| <a name="input_lambda_alarms"></a> [lambda\_alarms](#input\_lambda\_alarms) | n/a | <pre>map(object({<br>    metric_name         = string<br>    namespace           = string<br>    threshold           = number<br>    evaluation_periods  = number<br>    period              = number<br>    statistic           = string<br>    comparison_operator = string<br>    sns_topic_alarm_arn = string<br>    treat_missing_data  = string<br>  }))</pre> | n/a | yes |
| <a name="input_metadata_lambda"></a> [metadata\_lambda](#input\_metadata\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    table_client_registrations_arn    = string<br>    environment_variables             = map(string)<br>    vpc_id                            = string<br>    vpc_subnet_ids                    = list(string)<br>    vpc_endpoint_dynamodb_prefix_id   = string<br>    vpc_endpoint_ssm_nsg_ids          = list(string)<br>    cloudwatch_logs_retention_in_days = number<br>  })</pre> | n/a | yes |
| <a name="input_nlb_name"></a> [nlb\_name](#input\_nlb\_name) | Network load balancer name | `string` | n/a | yes |
| <a name="input_private_subnets"></a> [private\_subnets](#input\_private\_subnets) | Private subnets ids. | `list(string)` | n/a | yes |
| <a name="input_role_prefix"></a> [role\_prefix](#input\_role\_prefix) | IAM Role prefix. | `string` | n/a | yes |
| <a name="input_service_core"></a> [service\_core](#input\_service\_core) | n/a | <pre>object({<br>    service_name           = string<br>    cpu                    = number<br>    memory                 = number<br>    enable_execute_command = optional(bool, true)<br>    container = object({<br>      name                = string<br>      cpu                 = number<br>      memory              = number<br>      image_name          = string<br>      image_version       = string<br>      containerPort       = number<br>      hostPort            = number<br>      logs_retention_days = number<br>    })<br>    autoscaling = object({<br>      enable        = bool<br>      desired_count = number<br>      min_capacity  = number<br>      max_capacity  = number<br>    })<br>    environment_variables = list(object({<br>      name  = string<br>      value = string<br>    }))<br>  })</pre> | n/a | yes |
| <a name="input_sns_topic_arn"></a> [sns\_topic\_arn](#input\_sns\_topic\_arn) | n/a | `string` | n/a | yes |
| <a name="input_ssm_cert_key"></a> [ssm\_cert\_key](#input\_ssm\_cert\_key) | TODO fix name | <pre>object({<br>    cert_pem = optional(string, "cert.pem")<br>    key_pem  = optional(string, "key.pem")<br>  })</pre> | n/a | yes |
| <a name="input_table_client_registrations_arn"></a> [table\_client\_registrations\_arn](#input\_table\_client\_registrations\_arn) | Dynamodb table client registrations arn. | `string` | n/a | yes |
| <a name="input_vpc_cidr_block"></a> [vpc\_cidr\_block](#input\_vpc\_cidr\_block) | VPC cidr block. | `string` | n/a | yes |
| <a name="input_vpc_id"></a> [vpc\_id](#input\_vpc\_id) | VPC id | `string` | n/a | yes |
| <a name="input_dynamodb_table_stream_arn"></a> [dynamodb\_table\_stream\_arn](#input\_dynamodb\_table\_stream\_arn) | n/a | `string` | `null` | no |
| <a name="input_enable_container_insights"></a> [enable\_container\_insights](#input\_enable\_container\_insights) | ECS enable container insight. | `bool` | `true` | no |
| <a name="input_eventbridge_pipe_sessions"></a> [eventbridge\_pipe\_sessions](#input\_eventbridge\_pipe\_sessions) | n/a | <pre>object({<br>    pipe_name                     = string<br>    kms_sessions_table_alias      = string<br>    maximum_retry_attempts        = number<br>    maximum_record_age_in_seconds = number<br>  })</pre> | `null` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_assertion_lambda_arn"></a> [assertion\_lambda\_arn](#output\_assertion\_lambda\_arn) | # Metadata lambda ## |
| <a name="output_client_registration_lambda_arn"></a> [client\_registration\_lambda\_arn](#output\_client\_registration\_lambda\_arn) | # Client registration lambda |
| <a name="output_ecr_endpoints"></a> [ecr\_endpoints](#output\_ecr\_endpoints) | n/a |
| <a name="output_ecs_cluster_arn"></a> [ecs\_cluster\_arn](#output\_ecs\_cluster\_arn) | n/a |
| <a name="output_ecs_cluster_name"></a> [ecs\_cluster\_name](#output\_ecs\_cluster\_name) | n/a |
| <a name="output_ecs_deploy_iam_role_arn"></a> [ecs\_deploy\_iam\_role\_arn](#output\_ecs\_deploy\_iam\_role\_arn) | n/a |
| <a name="output_ecs_service_name"></a> [ecs\_service\_name](#output\_ecs\_service\_name) | n/a |
| <a name="output_elb"></a> [elb](#output\_elb) | n/a |
| <a name="output_jwt_sign_aliases"></a> [jwt\_sign\_aliases](#output\_jwt\_sign\_aliases) | n/a |
| <a name="output_lambda_deploy_iam_role_arn"></a> [lambda\_deploy\_iam\_role\_arn](#output\_lambda\_deploy\_iam\_role\_arn) | n/a |
| <a name="output_metadata_lambda_arn"></a> [metadata\_lambda\_arn](#output\_metadata\_lambda\_arn) | n/a |
| <a name="output_metadata_lambda_name"></a> [metadata\_lambda\_name](#output\_metadata\_lambda\_name) | TODO get the name from the arn |
| <a name="output_nlb_arn"></a> [nlb\_arn](#output\_nlb\_arn) | # Network loadbalancer ## |
| <a name="output_nlb_arn_suffix"></a> [nlb\_arn\_suffix](#output\_nlb\_arn\_suffix) | n/a |
| <a name="output_nlb_dns_name"></a> [nlb\_dns\_name](#output\_nlb\_dns\_name) | n/a |
| <a name="output_nlb_target_group_suffix_arn"></a> [nlb\_target\_group\_suffix\_arn](#output\_nlb\_target\_group\_suffix\_arn) | n/a |

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
| <a name="provider_random"></a> [random](#provider\_random) | n/a |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_assertion_lambda"></a> [assertion\_lambda](#module\_assertion\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_client_registration_lambda"></a> [client\_registration\_lambda](#module\_client\_registration\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_ecr"></a> [ecr](#module\_ecr) | terraform-aws-modules/ecr/aws | 1.6.0 |
| <a name="module_ecs_cluster"></a> [ecs\_cluster](#module\_ecs\_cluster) | terraform-aws-modules/ecs/aws | 5.9.1 |
| <a name="module_ecs_core_service"></a> [ecs\_core\_service](#module\_ecs\_core\_service) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |
| <a name="module_elb"></a> [elb](#module\_elb) | terraform-aws-modules/alb/aws | 9.8.0 |
| <a name="module_idp_metadata_lambda"></a> [idp\_metadata\_lambda](#module\_idp\_metadata\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_is_gh_integration_lambda"></a> [is\_gh\_integration\_lambda](#module\_is\_gh\_integration\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_jwt_sign"></a> [jwt\_sign](#module\_jwt\_sign) | terraform-aws-modules/kms/aws | 2.2.1 |
| <a name="module_kms_key_pem"></a> [kms\_key\_pem](#module\_kms\_key\_pem) | terraform-aws-modules/kms/aws | 3.0.0 |
| <a name="module_metadata_lambda"></a> [metadata\_lambda](#module\_metadata\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_retrieve_status_lambda"></a> [retrieve\_status\_lambda](#module\_retrieve\_status\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_s3_lambda_code_bucket"></a> [s3\_lambda\_code\_bucket](#module\_s3\_lambda\_code\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |
| <a name="module_security_group_lambda_assertion"></a> [security\_group\_lambda\_assertion](#module\_security\_group\_lambda\_assertion) | terraform-aws-modules/security-group/aws | 4.17.2 |
| <a name="module_security_group_lambda_client_registration"></a> [security\_group\_lambda\_client\_registration](#module\_security\_group\_lambda\_client\_registration) | terraform-aws-modules/security-group/aws | 4.17.2 |
| <a name="module_security_group_lambda_idp_metadata"></a> [security\_group\_lambda\_idp\_metadata](#module\_security\_group\_lambda\_idp\_metadata) | terraform-aws-modules/security-group/aws | 4.17.2 |
| <a name="module_security_group_lambda_metadata"></a> [security\_group\_lambda\_metadata](#module\_security\_group\_lambda\_metadata) | terraform-aws-modules/security-group/aws | 4.17.2 |
| <a name="module_security_group_retrieve_status_lambda"></a> [security\_group\_retrieve\_status\_lambda](#module\_security\_group\_retrieve\_status\_lambda) | terraform-aws-modules/security-group/aws | 4.17.2 |
| <a name="module_security_group_update_status_lambda"></a> [security\_group\_update\_status\_lambda](#module\_security\_group\_update\_status\_lambda) | terraform-aws-modules/security-group/aws | 4.17.2 |
| <a name="module_update_status_lambda"></a> [update\_status\_lambda](#module\_update\_status\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |

## Resources

| Name | Type |
|------|------|
| [aws_cloudwatch_event_rule.cert_key_changes](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_event_rule) | resource |
| [aws_cloudwatch_event_target.metadata_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_event_target) | resource |
| [aws_cloudwatch_log_group.ecs_core](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_log_group) | resource |
| [aws_cloudwatch_metric_alarm.client_error_alarm](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_cloudwatch_metric_alarm.dlq_assertions](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_cloudwatch_metric_alarm.dlq_sessions](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_cloudwatch_metric_alarm.ecs_alarms](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_cloudwatch_metric_alarm.idp_error_alarm](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_cloudwatch_metric_alarm.lambda_errors](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_metric_alarm) | resource |
| [aws_iam_policy.deploy_ecs](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_policy.deploy_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_policy.ecs_core_task](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_policy.switch_region_policy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_role.github_lambda_deploy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role.githubecsdeploy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role.pipe_sessions](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role.switch_region_role](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role_policy.pipe_source](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy) | resource |
| [aws_iam_role_policy_attachment.deploy_ecs](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.deploy_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.switch_region](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_lambda_event_source_mapping.trigger](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_event_source_mapping) | resource |
| [aws_lambda_permission.cert_key_changes](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_permission) | resource |
| [aws_pipes_pipe.sessions](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/pipes_pipe) | resource |
| [aws_s3_bucket_notification.bucket_notification](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_notification) | resource |
| [aws_security_group_rule.metadata_vpc_tls](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/security_group_rule) | resource |
| [aws_sns_topic_subscription.is-gh-integration](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/sns_topic_subscription) | resource |
| [aws_sqs_queue.dlq_lambda_assertion](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/sqs_queue) | resource |
| [aws_sqs_queue.pipe_dlq](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/sqs_queue) | resource |
| [aws_ssm_parameter.key_pem](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ssm_parameter) | resource |
| [random_integer.bucket_lambda_code_suffix](https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/integer) | resource |
| [aws_iam_policy_document.assertion_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.client_registration_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.idp_metadata_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.is_gh_integration_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.metadata_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.retrieve_status_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.update_status_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_ssm_parameter.certificate](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/ssm_parameter) | data source |
| [aws_ssm_parameter.is_gh_integration_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/ssm_parameter) | data source |
| [aws_ssm_parameter.key](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/ssm_parameter) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_account_id"></a> [account\_id](#input\_account\_id) | AWS Account id. | `string` | n/a | yes |
| <a name="input_assertion_lambda"></a> [assertion\_lambda](#input\_assertion\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    s3_assertion_bucket_arn           = string<br>    kms_assertion_key_arn             = string<br>    environment_variables             = map(string)<br>    cloudwatch_logs_retention_in_days = number<br>    vpc_s3_prefix_id                  = string<br>    vpc_subnet_ids                    = list(string)<br>    vpc_id                            = string<br>  })</pre> | n/a | yes |
| <a name="input_aws_caller_identity"></a> [aws\_caller\_identity](#input\_aws\_caller\_identity) | n/a | `string` | `""` | no |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS Region. | `string` | n/a | yes |
| <a name="input_client_alarm"></a> [client\_alarm](#input\_client\_alarm) | n/a | <pre>object({<br>    namespace = string<br>    client_id = list(string)<br>  })</pre> | `null` | no |
| <a name="input_client_registration_lambda"></a> [client\_registration\_lambda](#input\_client\_registration\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    table_client_registrations_arn    = string<br>    cloudwatch_logs_retention_in_days = number<br>    vpc_id                            = string<br>    vpc_endpoint_dynamodb_prefix_id   = string<br>    vpc_subnet_ids                    = list(string)<br>    environment_variables             = map(string)<br>  })</pre> | n/a | yes |
| <a name="input_dlq_alarms"></a> [dlq\_alarms](#input\_dlq\_alarms) | n/a | <pre>object({<br>    metric_name         = string<br>    namespace           = string<br>    threshold           = number<br>    evaluation_periods  = number<br>    period              = number<br>    statistic           = string<br>    comparison_operator = string<br>    sns_topic_alarm_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_dynamodb_clients_table_stream_arn"></a> [dynamodb\_clients\_table\_stream\_arn](#input\_dynamodb\_clients\_table\_stream\_arn) | n/a | `string` | `null` | no |
| <a name="input_dynamodb_table_clientStatus"></a> [dynamodb\_table\_clientStatus](#input\_dynamodb\_table\_clientStatus) | Dynamodb table clientStatus arns | <pre>object({<br>    table_arn       = string<br>    gsi_pointer_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_dynamodb_table_idpMetadata"></a> [dynamodb\_table\_idpMetadata](#input\_dynamodb\_table\_idpMetadata) | Dynamodb table idpMetadata anrs | <pre>object({<br>    table_arn       = string<br>    gsi_pointer_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_dynamodb_table_idpStatus"></a> [dynamodb\_table\_idpStatus](#input\_dynamodb\_table\_idpStatus) | Dynamodb table idpStatus arns | <pre>object({<br>    table_arn       = string<br>    gsi_pointer_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_dynamodb_table_sessions"></a> [dynamodb\_table\_sessions](#input\_dynamodb\_table\_sessions) | Dynamodb table sessions anrs | <pre>object({<br>    table_arn    = string<br>    gsi_code_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_dynamodb_table_stream_arn"></a> [dynamodb\_table\_stream\_arn](#input\_dynamodb\_table\_stream\_arn) | n/a | `string` | `null` | no |
| <a name="input_ecr_registers"></a> [ecr\_registers](#input\_ecr\_registers) | ECR image repositories | <pre>list(object({<br>    name                            = string<br>    number_of_images_to_keep        = number<br>    repository_image_tag_mutability = optional(string, "IMMUTABLE")<br>  }))</pre> | n/a | yes |
| <a name="input_ecs_alarms"></a> [ecs\_alarms](#input\_ecs\_alarms) | n/a | <pre>map(object({<br>    metric_name         = string<br>    namespace           = string<br>    threshold           = number<br>    evaluation_periods  = number<br>    period              = number<br>    statistic           = string<br>    comparison_operator = string<br>    sns_topic_alarm_arn = string<br>    scaling_policy      = optional(string, null)<br>  }))</pre> | n/a | yes |
| <a name="input_ecs_cluster_name"></a> [ecs\_cluster\_name](#input\_ecs\_cluster\_name) | ECS Cluster name | `string` | n/a | yes |
| <a name="input_enable_container_insights"></a> [enable\_container\_insights](#input\_enable\_container\_insights) | ECS enable container insight. | `bool` | `true` | no |
| <a name="input_eventbridge_pipe_sessions"></a> [eventbridge\_pipe\_sessions](#input\_eventbridge\_pipe\_sessions) | n/a | <pre>object({<br>    pipe_name                     = string<br>    kms_sessions_table_alias      = string<br>    maximum_retry_attempts        = number<br>    maximum_record_age_in_seconds = number<br>  })</pre> | `null` | no |
| <a name="input_fargate_capacity_providers"></a> [fargate\_capacity\_providers](#input\_fargate\_capacity\_providers) | n/a | <pre>map(object({<br>    default_capacity_provider_strategy = object({<br>      weight = number<br>      base   = number<br>    })<br>  }))</pre> | n/a | yes |
| <a name="input_github_repository"></a> [github\_repository](#input\_github\_repository) | Github repository responsible to deploy ECS tasks in the form <organization\|user/repository>. | `string` | n/a | yes |
| <a name="input_hosted_zone_id"></a> [hosted\_zone\_id](#input\_hosted\_zone\_id) | Hosted zone id for IAM Role | `string` | `"Z065844519UG4CA4QH19U"` | no |
| <a name="input_idp_alarm"></a> [idp\_alarm](#input\_idp\_alarm) | n/a | <pre>object({<br>    namespace = string<br>    entity_id = list(string)<br>  })</pre> | `null` | no |
| <a name="input_idp_metadata_lambda"></a> [idp\_metadata\_lambda](#input\_idp\_metadata\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    environment_variables             = map(string)<br>    s3_idp_metadata_bucket_arn        = string<br>    s3_idp_metadata_bucket_id         = string<br>    vpc_id                            = string<br>    vpc_subnet_ids                    = list(string)<br>    vpc_s3_prefix_id                  = string<br>    cloudwatch_logs_retention_in_days = number<br>  })</pre> | n/a | yes |
| <a name="input_is_gh_integration_lambda"></a> [is\_gh\_integration\_lambda](#input\_is\_gh\_integration\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    sns_topic_arn                     = optional(string, null)<br>    cloudwatch_logs_retention_in_days = string<br>    ssm_parameter_name                = optional(string, "GH_PERSONAL_ACCESS_TOKEN")<br>    environment_variables             = map(string)<br>  })</pre> | n/a | yes |
| <a name="input_kms_rotation_period_in_days"></a> [kms\_rotation\_period\_in\_days](#input\_kms\_rotation\_period\_in\_days) | n/a | `number` | `365` | no |
| <a name="input_kms_sessions_table_alias_arn"></a> [kms\_sessions\_table\_alias\_arn](#input\_kms\_sessions\_table\_alias\_arn) | Kms key used to encrypt and decrypt session table. | `string` | n/a | yes |
| <a name="input_kms_ssm_enable_rotation"></a> [kms\_ssm\_enable\_rotation](#input\_kms\_ssm\_enable\_rotation) | n/a | `bool` | `true` | no |
| <a name="input_lambda_alarms"></a> [lambda\_alarms](#input\_lambda\_alarms) | n/a | <pre>map(object({<br>    metric_name         = string<br>    namespace           = string<br>    threshold           = number<br>    evaluation_periods  = number<br>    period              = number<br>    statistic           = string<br>    comparison_operator = string<br>    sns_topic_alarm_arn = string<br>    treat_missing_data  = string<br>  }))</pre> | n/a | yes |
| <a name="input_lambda_client_registration_trigger_enabled"></a> [lambda\_client\_registration\_trigger\_enabled](#input\_lambda\_client\_registration\_trigger\_enabled) | n/a | `bool` | `true` | no |
| <a name="input_metadata_lambda"></a> [metadata\_lambda](#input\_metadata\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    table_client_registrations_arn    = string<br>    environment_variables             = map(string)<br>    assets_bucket_arn                 = string<br>    vpc_id                            = string<br>    vpc_subnet_ids                    = list(string)<br>    vpc_endpoint_dynamodb_prefix_id   = string<br>    vpc_s3_prefix_id                  = string<br>    vpc_endpoint_ssm_nsg_ids          = list(string)<br>    cloudwatch_logs_retention_in_days = number<br>  })</pre> | n/a | yes |
| <a name="input_nlb_name"></a> [nlb\_name](#input\_nlb\_name) | Network load balancer name | `string` | n/a | yes |
| <a name="input_private_subnets"></a> [private\_subnets](#input\_private\_subnets) | Private subnets ids. | `list(string)` | n/a | yes |
| <a name="input_rest_api_id"></a> [rest\_api\_id](#input\_rest\_api\_id) | n/a | `string` | n/a | yes |
| <a name="input_retrieve_status_lambda"></a> [retrieve\_status\_lambda](#input\_retrieve\_status\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    cloudwatch_logs_retention_in_days = string<br>    environment_variables             = map(string)<br>    vpc_endpoint_dynamodb_prefix_id   = string<br>    vpc_subnet_ids                    = list(string)<br>    vpc_id                            = string<br>  })</pre> | n/a | yes |
| <a name="input_role_prefix"></a> [role\_prefix](#input\_role\_prefix) | IAM Role prefix. | `string` | n/a | yes |
| <a name="input_service_core"></a> [service\_core](#input\_service\_core) | n/a | <pre>object({<br>    service_name           = string<br>    cpu                    = number<br>    memory                 = number<br>    enable_execute_command = optional(bool, true)<br>    container = object({<br>      name                = string<br>      cpu                 = number<br>      memory              = number<br>      image_name          = string<br>      image_version       = string<br>      containerPort       = number<br>      hostPort            = number<br>      logs_retention_days = number<br>    })<br>    autoscaling = object({<br>      enable        = bool<br>      desired_count = number<br>      min_capacity  = number<br>      max_capacity  = number<br>    })<br>    environment_variables = list(object({<br>      name  = string<br>      value = string<br>    }))<br>  })</pre> | n/a | yes |
| <a name="input_sns_topic_arn"></a> [sns\_topic\_arn](#input\_sns\_topic\_arn) | n/a | `string` | n/a | yes |
| <a name="input_ssm_cert_key"></a> [ssm\_cert\_key](#input\_ssm\_cert\_key) | TODO fix name | <pre>object({<br>    cert_pem = optional(string, "cert.pem")<br>    key_pem  = optional(string, "key.pem")<br>  })</pre> | n/a | yes |
| <a name="input_switch_region_enabled"></a> [switch\_region\_enabled](#input\_switch\_region\_enabled) | n/a | `bool` | `false` | no |
| <a name="input_table_client_registrations_arn"></a> [table\_client\_registrations\_arn](#input\_table\_client\_registrations\_arn) | Dynamodb table client registrations arn. | `string` | n/a | yes |
| <a name="input_update_status_lambda"></a> [update\_status\_lambda](#input\_update\_status\_lambda) | n/a | <pre>object({<br>    name                              = string<br>    filename                          = string<br>    assets_bucket_arn                 = string<br>    cloudwatch_logs_retention_in_days = string<br>    environment_variables             = map(string)<br>    vpc_s3_prefix_id                  = string<br>    vpc_endpoint_dynamodb_prefix_id   = string<br>    vpc_subnet_ids                    = list(string)<br>    vpc_id                            = string<br>  })</pre> | n/a | yes |
| <a name="input_vpc_cidr_block"></a> [vpc\_cidr\_block](#input\_vpc\_cidr\_block) | VPC cidr block. | `string` | n/a | yes |
| <a name="input_vpc_id"></a> [vpc\_id](#input\_vpc\_id) | VPC id | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_assertion_lambda_arn"></a> [assertion\_lambda\_arn](#output\_assertion\_lambda\_arn) | # Metadata lambda ## |
| <a name="output_client_registration_lambda_arn"></a> [client\_registration\_lambda\_arn](#output\_client\_registration\_lambda\_arn) | # Client registration lambda |
| <a name="output_ecr_endpoints"></a> [ecr\_endpoints](#output\_ecr\_endpoints) | n/a |
| <a name="output_ecs_cluster_arn"></a> [ecs\_cluster\_arn](#output\_ecs\_cluster\_arn) | n/a |
| <a name="output_ecs_cluster_name"></a> [ecs\_cluster\_name](#output\_ecs\_cluster\_name) | n/a |
| <a name="output_ecs_core_log_group_name"></a> [ecs\_core\_log\_group\_name](#output\_ecs\_core\_log\_group\_name) | n/a |
| <a name="output_ecs_deploy_iam_role_arn"></a> [ecs\_deploy\_iam\_role\_arn](#output\_ecs\_deploy\_iam\_role\_arn) | n/a |
| <a name="output_ecs_service_name"></a> [ecs\_service\_name](#output\_ecs\_service\_name) | n/a |
| <a name="output_elb"></a> [elb](#output\_elb) | n/a |
| <a name="output_jwt_sign_aliases"></a> [jwt\_sign\_aliases](#output\_jwt\_sign\_aliases) | n/a |
| <a name="output_lambda_deploy_iam_role_arn"></a> [lambda\_deploy\_iam\_role\_arn](#output\_lambda\_deploy\_iam\_role\_arn) | n/a |
| <a name="output_metadata_lambda_arn"></a> [metadata\_lambda\_arn](#output\_metadata\_lambda\_arn) | n/a |
| <a name="output_metadata_lambda_name"></a> [metadata\_lambda\_name](#output\_metadata\_lambda\_name) | TODO get the name from the arn |
| <a name="output_nlb_arn"></a> [nlb\_arn](#output\_nlb\_arn) | # Network loadbalancer ## |
| <a name="output_nlb_arn_suffix"></a> [nlb\_arn\_suffix](#output\_nlb\_arn\_suffix) | n/a |
| <a name="output_nlb_dns_name"></a> [nlb\_dns\_name](#output\_nlb\_dns\_name) | n/a |
| <a name="output_nlb_target_group_suffix_arn"></a> [nlb\_target\_group\_suffix\_arn](#output\_nlb\_target\_group\_suffix\_arn) | n/a |
| <a name="output_retrieve_status_lambda_arn"></a> [retrieve\_status\_lambda\_arn](#output\_retrieve\_status\_lambda\_arn) | # Retrieve status lambda |
| <a name="output_s3_lambda_code_bucket"></a> [s3\_lambda\_code\_bucket](#output\_s3\_lambda\_code\_bucket) | # S3 buket for lambda code ## |
<!-- END_TF_DOCS -->