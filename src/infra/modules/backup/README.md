<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.49 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 5.49 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_aws_backup"></a> [aws\_backup](#module\_aws\_backup) | git::https://github.com/pagopa/terraform-aws-backup.git | v1.1.0 |

## Resources

| Name | Type |
|------|------|
| [aws_iam_policy.backup](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_policy) | resource |
| [aws_iam_role.backup](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_role) | resource |
| [aws_iam_role_policy.backup](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_role_policy) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_prefix"></a> [prefix](#input\_prefix) | n/a | `string` | `"Prefix to assign to the resources."` | no |

## Outputs

No outputs.
<!-- END_TF_DOCS -->