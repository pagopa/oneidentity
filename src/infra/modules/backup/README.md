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

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_aws_backup"></a> [aws\_backup](#module\_aws\_backup) | git::https://github.com/pagopa/terraform-aws-backup.git | v1.3.4 |

## Resources

| Name | Type |
|------|------|
| [aws_iam_policy.backup](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_role.backup](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role_policy.backup](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_backup_name"></a> [backup\_name](#input\_backup\_name) | Backup name. This name will be use to assign a name to the vault and the plane | `string` | n/a | yes |
| <a name="input_backup_rule"></a> [backup\_rule](#input\_backup\_rule) | A rule object that specifies a scheduled task that is used to back up a selection of resources | `any` | n/a | yes |
| <a name="input_prefix"></a> [prefix](#input\_prefix) | n/a | `string` | `"Prefix to assign to the resources."` | no |

## Outputs

No outputs.
<!-- END_TF_DOCS -->