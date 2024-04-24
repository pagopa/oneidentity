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
| <a name="module_ecr"></a> [ecr](#module\_ecr) | terraform-aws-modules/ecr/aws | 1.6.0 |
| <a name="module_ecs"></a> [ecs](#module\_ecs) | terraform-aws-modules/ecs/aws | 5.9.1 |
| <a name="module_ecs_idp_service"></a> [ecs\_idp\_service](#module\_ecs\_idp\_service) | terraform-aws-modules/ecs/aws//modules/service | 5.9.1 |

## Resources

| Name | Type |
|------|------|
| [aws_iam_policy.deploy_ecs](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_role.githubecsdeploy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role_policy_attachment.deploy_ecs](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy_attachment) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_account_id"></a> [account\_id](#input\_account\_id) | AWS Account id. | `string` | n/a | yes |
| <a name="input_ecr_registers"></a> [ecr\_registers](#input\_ecr\_registers) | ECR image repositories | <pre>list(object({<br>    name                     = string<br>    number_of_images_to_keep = number<br>  }))</pre> | n/a | yes |
| <a name="input_ecs_cluster_name"></a> [ecs\_cluster\_name](#input\_ecs\_cluster\_name) | ECS Cluster name | `string` | n/a | yes |
| <a name="input_fargate_capacity_providers"></a> [fargate\_capacity\_providers](#input\_fargate\_capacity\_providers) | n/a | <pre>map(object({<br>    default_capacity_provider_strategy = object({<br>      weight = number<br>      base   = number<br>    })<br>  }))</pre> | n/a | yes |
| <a name="input_github_repository"></a> [github\_repository](#input\_github\_repository) | Github repository responsible to deploy ECS tasks in the form <organization\|user/repository>. | `string` | n/a | yes |
| <a name="input_service_idp"></a> [service\_idp](#input\_service\_idp) | n/a | <pre>object({<br>    service_name           = string<br>    cpu                    = number<br>    memory                 = number<br>    enable_execute_command = optional(bool, true)<br>    container = object({<br>      name          = string<br>      cpu           = number<br>      memory        = number<br>      image_name    = string<br>      image_version = string<br>      containerPort = number<br>      hostPort      = number<br>    })<br>    autoscaling = object({<br>      enable       = bool<br>      min_capacity = number<br>      max_capacity = number<br>    })<br><br>    subnet_ids = list(string)<br><br>    load_balancer = object({<br>      target_group_arn  = string<br>      security_group_id = string<br>    })<br><br>  })</pre> | n/a | yes |
| <a name="input_enable_container_insights"></a> [enable\_container\_insights](#input\_enable\_container\_insights) | ECS enable container insight. | `bool` | `true` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_ecr_endpoints"></a> [ecr\_endpoints](#output\_ecr\_endpoints) | n/a |
| <a name="output_ecs_cluster_name"></a> [ecs\_cluster\_name](#output\_ecs\_cluster\_name) | n/a |
| <a name="output_ecs_deploy_iam_role_arn"></a> [ecs\_deploy\_iam\_role\_arn](#output\_ecs\_deploy\_iam\_role\_arn) | n/a |
