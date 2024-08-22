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
| [aws_iam_openid_connect_provider.github](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_openid_connect_provider) | resource |
| [aws_iam_role.githubiac](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role_policy_attachment.githubiac](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy_attachment) | resource |
| [aws_caller_identity.current](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/caller_identity) | data source |
| [aws_iam_policy.admin_access](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_github_repository"></a> [github\_repository](#input\_github\_repository) | Github federation repository | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_iac_github_role_arn"></a> [iac\_github\_role\_arn](#output\_iac\_github\_role\_arn) | Role github can assume to build the infrastructure. |
<!-- END_TF_DOCS -->