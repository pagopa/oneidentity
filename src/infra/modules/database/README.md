## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38 |

## Providers

No providers.

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_dynamodb_table_saml_responses"></a> [dynamodb\_table\_saml\_responses](#module\_dynamodb\_table\_saml\_responses) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_kms_table_saml_responses"></a> [kms\_table\_saml\_responses](#module\_kms\_table\_saml\_responses) | terraform-aws-modules/kms/aws | 2.2.1 |

## Resources

No resources.

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_saml_responses_table"></a> [saml\_responses\_table](#input\_saml\_responses\_table) | Saml responses table configurations. | <pre>object({<br>    name                           = string<br>    ttl_enabled                    = optional(bool, true)<br>    point_in_time_recovery_enabled = optional(bool, false)<br>  })</pre> | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_saml_responses_table_name"></a> [saml\_responses\_table\_name](#output\_saml\_responses\_table\_name) | n/a |
