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
| <a name="module_client_registration_lambda"></a> [client\_registration\_lambda](#module\_client\_registration\_lambda) | terraform-aws-modules/lambda/aws | 7.4.0 |
| <a name="module_ecr"></a> [ecr](#module\_ecr) | terraform-aws-modules/ecr/aws | 1.6.0 |
| <a name="module_ecs"></a> [ecs](#module\_ecs) | terraform-aws-modules/ecs/aws | 5.9.1 |
| <a name="module_ecs_core_service"></a> [ecs\_core\_service](#module\_ecs\_core\_service) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |
| <a name="module_elb"></a> [elb](#module\_elb) | terraform-aws-modules/alb/aws | 9.8.0 |
| <a name="module_jwt_sign"></a> [jwt\_sign](#module\_jwt\_sign) | terraform-aws-modules/kms/aws | 2.2.1 |

## Resources

| Name | Type |
|------|------|
| [aws_iam_policy.deploy_ecs](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_policy.ecs_core_task](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_role.githubecsdeploy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role_policy_attachment.deploy_ecs](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_policy_document.client_registration_lambda](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_account_id"></a> [account\_id](#input\_account\_id) | AWS Account id. | `string` | n/a | yes |
| <a name="input_client_registration_lambda"></a> [client\_registration\_lambda](#input\_client\_registration\_lambda) | n/a | <pre>object({<br>    name                           = string<br>    filename                       = string<br>    table_client_registrations_arn = string<br>  })</pre> | n/a | yes |
| <a name="input_ecr_registers"></a> [ecr\_registers](#input\_ecr\_registers) | ECR image repositories | <pre>list(object({<br>    name                            = string<br>    number_of_images_to_keep        = number<br>    repository_image_tag_mutability = optional(string, "IMMUTABLE")<br>  }))</pre> | n/a | yes |
| <a name="input_ecs_cluster_name"></a> [ecs\_cluster\_name](#input\_ecs\_cluster\_name) | ECS Cluster name | `string` | n/a | yes |
| <a name="input_fargate_capacity_providers"></a> [fargate\_capacity\_providers](#input\_fargate\_capacity\_providers) | n/a | <pre>map(object({<br>    default_capacity_provider_strategy = object({<br>      weight = number<br>      base   = number<br>    })<br>  }))</pre> | n/a | yes |
| <a name="input_github_repository"></a> [github\_repository](#input\_github\_repository) | Github repository responsible to deploy ECS tasks in the form <organization\|user/repository>. | `string` | n/a | yes |
| <a name="input_nlb_name"></a> [nlb\_name](#input\_nlb\_name) | Network load balancer name | `string` | n/a | yes |
| <a name="input_private_subnets"></a> [private\_subnets](#input\_private\_subnets) | Private subnets ids. | `list(string)` | n/a | yes |
| <a name="input_service_core"></a> [service\_core](#input\_service\_core) | n/a | <pre>object({<br>    service_name           = string<br>    cpu                    = number<br>    memory                 = number<br>    enable_execute_command = optional(bool, true)<br>    container = object({<br>      name          = string<br>      cpu           = number<br>      memory        = number<br>      image_name    = string<br>      image_version = string<br>      containerPort = number<br>      hostPort      = number<br>    })<br>    autoscaling = object({<br>      enable       = bool<br>      min_capacity = number<br>      max_capacity = number<br>    })<br>  })</pre> | n/a | yes |
| <a name="input_table_saml_responces_arn"></a> [table\_saml\_responces\_arn](#input\_table\_saml\_responces\_arn) | Dynamodb table saml responses arn. | `string` | n/a | yes |
| <a name="input_vpc_cidr_block"></a> [vpc\_cidr\_block](#input\_vpc\_cidr\_block) | VPC cidr block. | `string` | n/a | yes |
| <a name="input_vpc_id"></a> [vpc\_id](#input\_vpc\_id) | VPC id | `string` | n/a | yes |
| <a name="input_enable_container_insights"></a> [enable\_container\_insights](#input\_enable\_container\_insights) | ECS enable container insight. | `bool` | `true` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_ecr_endpoints"></a> [ecr\_endpoints](#output\_ecr\_endpoints) | n/a |
| <a name="output_ecs_cluster_name"></a> [ecs\_cluster\_name](#output\_ecs\_cluster\_name) | n/a |
| <a name="output_ecs_deploy_iam_role_arn"></a> [ecs\_deploy\_iam\_role\_arn](#output\_ecs\_deploy\_iam\_role\_arn) | n/a |
| <a name="output_jwt_sign_aliases"></a> [jwt\_sign\_aliases](#output\_jwt\_sign\_aliases) | n/a |
| <a name="output_nlb_arn"></a> [nlb\_arn](#output\_nlb\_arn) | # Network loadbalancer ## |
| <a name="output_nlb_dns_name"></a> [nlb\_dns\_name](#output\_nlb\_dns\_name) | n/a |
