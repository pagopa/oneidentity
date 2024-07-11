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
| <a name="module_ecs"></a> [ecs](#module\_ecs) | terraform-aws-modules/ecs/aws | 5.9.1 |
| <a name="module_ecs_core_service"></a> [ecs\_core\_service](#module\_ecs\_core\_service) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |
| <a name="module_ecs_spid_validator"></a> [ecs\_spid\_validator](#module\_ecs\_spid\_validator) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |
| <a name="module_elb"></a> [elb](#module\_elb) | terraform-aws-modules/alb/aws | 9.8.0 |
| <a name="module_jwt_sign"></a> [jwt\_sign](#module\_jwt\_sign) | terraform-aws-modules/kms/aws | 2.2.1 |
| <a name="module_metadata_lambda"></a> [metadata\_lambda](#module\_metadata\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |

## Resources

| Name | Type |
|------|------|
| [aws_cloudwatch_log_group.ecs_core](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_log_group) | resource |
| [aws_cloudwatch_log_group.ecs_spid_validator](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/cloudwatch_log_group) | resource |
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
| [aws_iam_policy_document.client_registration_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.metadata_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_account_id"></a> [account\_id](#input\_account\_id) | AWS Account id. | `string` | n/a | yes |
| <a name="input_assertion_lambda"></a> [assertion\_lambda](#input\_assertion\_lambda) | n/a | <pre>object({<br>    name        = string,<br>    source_path = string<br>  })</pre> | n/a | yes |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS Region. | `string` | n/a | yes |
| <a name="input_client_registration_lambda"></a> [client\_registration\_lambda](#input\_client\_registration\_lambda) | n/a | <pre>object({<br>    name                           = string<br>    filename                       = string<br>    table_client_registrations_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_dynamodb_table_sessions"></a> [dynamodb\_table\_sessions](#input\_dynamodb\_table\_sessions) | Dynamodb table sessions anrs | <pre>object({<br>    table_arn    = string<br>    gsi_code_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_ecr_registers"></a> [ecr\_registers](#input\_ecr\_registers) | ECR image repositories | <pre>list(object({<br>    name                            = string<br>    number_of_images_to_keep        = number<br>    repository_image_tag_mutability = optional(string, "IMMUTABLE")<br>  }))</pre> | n/a | yes |
| <a name="input_ecs_cluster_name"></a> [ecs\_cluster\_name](#input\_ecs\_cluster\_name) | ECS Cluster name | `string` | n/a | yes |
| <a name="input_fargate_capacity_providers"></a> [fargate\_capacity\_providers](#input\_fargate\_capacity\_providers) | n/a | <pre>map(object({<br>    default_capacity_provider_strategy = object({<br>      weight = number<br>      base   = number<br>    })<br>  }))</pre> | n/a | yes |
| <a name="input_github_repository"></a> [github\_repository](#input\_github\_repository) | Github repository responsible to deploy ECS tasks in the form <organization\|user/repository>. | `string` | n/a | yes |
| <a name="input_kms_sessions_table_alias_arn"></a> [kms\_sessions\_table\_alias\_arn](#input\_kms\_sessions\_table\_alias\_arn) | Kms key used to encrypt and dectypt session table. | `string` | n/a | yes |
| <a name="input_metadata_lambda"></a> [metadata\_lambda](#input\_metadata\_lambda) | n/a | <pre>object({<br>    name                           = string<br>    filename                       = string<br>    table_client_registrations_arn = string<br>    environment_variables          = map(string)<br>  })</pre> | n/a | yes |
| <a name="input_nlb_name"></a> [nlb\_name](#input\_nlb\_name) | Network load balancer name | `string` | n/a | yes |
| <a name="input_private_subnets"></a> [private\_subnets](#input\_private\_subnets) | Private subnets ids. | `list(string)` | n/a | yes |
| <a name="input_service_core"></a> [service\_core](#input\_service\_core) | n/a | <pre>object({<br>    service_name           = string<br>    cpu                    = number<br>    memory                 = number<br>    enable_execute_command = optional(bool, true)<br>    container = object({<br>      name                = string<br>      cpu                 = number<br>      memory              = number<br>      image_name          = string<br>      image_version       = string<br>      containerPort       = number<br>      hostPort            = number<br>      logs_retention_days = number<br>    })<br>    autoscaling = object({<br>      enable       = bool<br>      min_capacity = number<br>      max_capacity = number<br>    })<br>    environment_variables = list(object({<br>      name  = string<br>      value = string<br>    }))<br>  })</pre> | n/a | yes |
| <a name="input_table_client_registrations_arn"></a> [table\_client\_registrations\_arn](#input\_table\_client\_registrations\_arn) | Dynamodb table client registrations arn. | `string` | n/a | yes |
| <a name="input_vpc_cidr_block"></a> [vpc\_cidr\_block](#input\_vpc\_cidr\_block) | VPC cidr block. | `string` | n/a | yes |
| <a name="input_vpc_id"></a> [vpc\_id](#input\_vpc\_id) | VPC id | `string` | n/a | yes |
| <a name="input_dynamodb_table_stream_arn"></a> [dynamodb\_table\_stream\_arn](#input\_dynamodb\_table\_stream\_arn) | n/a | `string` | `null` | no |
| <a name="input_enable_container_insights"></a> [enable\_container\_insights](#input\_enable\_container\_insights) | ECS enable container insight. | `bool` | `true` | no |
| <a name="input_eventbridge_pipe_sessions"></a> [eventbridge\_pipe\_sessions](#input\_eventbridge\_pipe\_sessions) | n/a | <pre>object({<br>    pipe_name                = string<br>    kms_sessions_table_alias = string<br>  })</pre> | `null` | no |
| <a name="input_spid_validator"></a> [spid\_validator](#input\_spid\_validator) | Spid validator configurations. When null the resources won't be created. | <pre>object({<br>    service_name = string<br>    cpu          = optional(number, 512)<br>    memory       = optional(number, 1024)<br>    container = object({<br>      name                = string<br>      image_name          = string<br>      image_version       = string<br>      cpu                 = optional(number, 512)<br>      memory              = optional(number, 1024)<br>      logs_retention_days = optional(number, 14)<br>    })<br>    alb_target_group_arn  = string<br>    alb_security_group_id = string<br><br>  })</pre> | `null` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_assertion_lambda_arn"></a> [assertion\_lambda\_arn](#output\_assertion\_lambda\_arn) | n/a |
| <a name="output_ecr_endpoints"></a> [ecr\_endpoints](#output\_ecr\_endpoints) | n/a |
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
