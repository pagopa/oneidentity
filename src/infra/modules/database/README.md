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
| <a name="module_dynamodb_sessions_table"></a> [dynamodb\_sessions\_table](#module\_dynamodb\_sessions\_table) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_dynamodb_table_client_registrations"></a> [dynamodb\_table\_client\_registrations](#module\_dynamodb\_table\_client\_registrations) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_kms_sessions_table"></a> [kms\_sessions\_table](#module\_kms\_sessions\_table) | terraform-aws-modules/kms/aws | 2.2.1 |

## Resources

No resources.

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_client_registrations_table"></a> [client\_registrations\_table](#input\_client\_registrations\_table) | Client registrations table configurations. | <pre>object({<br>    point_in_time_recovery_enabled = optional(bool, false)<br>  })</pre> | n/a | yes |
| <a name="input_sessions_table"></a> [sessions\_table](#input\_sessions\_table) | Saml responses table configurations. | <pre>object({<br>    ttl_enabled                    = optional(bool, true)<br>    point_in_time_recovery_enabled = optional(bool, false)<br>    stream_enabled                 = optional(bool, false)<br>    stream_view_type               = string<br>  })</pre> | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_dynamodb_table_stream_arn"></a> [dynamodb\_table\_stream\_arn](#output\_dynamodb\_table\_stream\_arn) | n/a |
| <a name="output_kms_sessions_table_alias_arn"></a> [kms\_sessions\_table\_alias\_arn](#output\_kms\_sessions\_table\_alias\_arn) | n/a |
| <a name="output_table_client_registrations_arn"></a> [table\_client\_registrations\_arn](#output\_table\_client\_registrations\_arn) | n/a |
| <a name="output_table_client_registrations_name"></a> [table\_client\_registrations\_name](#output\_table\_client\_registrations\_name) | n/a |
| <a name="output_table_sessions_arn"></a> [table\_sessions\_arn](#output\_table\_sessions\_arn) | n/a |
| <a name="output_table_sessions_gsi_code_arn"></a> [table\_sessions\_gsi\_code\_arn](#output\_table\_sessions\_gsi\_code\_arn) | n/a |
| <a name="output_table_sessions_name"></a> [table\_sessions\_name](#output\_table\_sessions\_name) | n/a |

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
| <a name="module_dynamodb_sessions_table"></a> [dynamodb\_sessions\_table](#module\_dynamodb\_sessions\_table) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_dynamodb_table_client_registrations"></a> [dynamodb\_table\_client\_registrations](#module\_dynamodb\_table\_client\_registrations) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_dynamodb_table_client_status_history"></a> [dynamodb\_table\_client\_status\_history](#module\_dynamodb\_table\_client\_status\_history) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_dynamodb_table_idpMetadata"></a> [dynamodb\_table\_idpMetadata](#module\_dynamodb\_table\_idpMetadata) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_dynamodb_table_idp_status_history"></a> [dynamodb\_table\_idp\_status\_history](#module\_dynamodb\_table\_idp\_status\_history) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_kms_sessions_table"></a> [kms\_sessions\_table](#module\_kms\_sessions\_table) | terraform-aws-modules/kms/aws | 3.0.0 |

## Resources

| Name | Type |
|------|------|
| [aws_dynamodb_table_item.default_client_status_history_item](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/dynamodb_table_item) | resource |
| [aws_dynamodb_table_item.default_idp_status_history_item](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/dynamodb_table_item) | resource |
| [aws_dynamodb_table.dynamodb_table_client_registrations](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/dynamodb_table) | data source |
| [aws_dynamodb_table.dynamodb_table_client_status_history](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/dynamodb_table) | data source |
| [aws_dynamodb_table.dynamodb_table_idp_status_history](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/dynamodb_table) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_client_registrations_table"></a> [client\_registrations\_table](#input\_client\_registrations\_table) | Client registrations table configurations. | <pre>object({<br/>    point_in_time_recovery_enabled = optional(bool, false)<br/>    stream_enabled                 = optional(bool, true)<br/>    stream_view_type               = optional(string, "NEW_AND_OLD_IMAGES")<br/>    deletion_protection_enabled    = optional(bool, false)<br/>    replication_regions = optional(list(object({<br/>      region_name            = string<br/>      propagate_tags         = optional(bool, true)<br/>      point_in_time_recovery = optional(bool, true)<br/>    })), [])<br/>  })</pre> | n/a | yes |
| <a name="input_client_status_history_table"></a> [client\_status\_history\_table](#input\_client\_status\_history\_table) | Client status history table configurations. | <pre>object({<br/>    point_in_time_recovery_enabled = optional(bool, false)<br/>    stream_enabled                 = optional(bool, false)<br/>    stream_view_type               = optional(string, null)<br/>    deletion_protection_enabled    = optional(bool, false)<br/>    replication_regions = optional(list(object({<br/>      region_name            = string<br/>      propagate_tags         = optional(bool, true)<br/>      point_in_time_recovery = optional(bool, true)<br/>    })), [])<br/>  })</pre> | n/a | yes |
| <a name="input_clients"></a> [clients](#input\_clients) | n/a | <pre>list(object({<br/>    client_id     = string<br/>    friendly_name = string<br/>  }))</pre> | `null` | no |
| <a name="input_idp_entity_ids"></a> [idp\_entity\_ids](#input\_idp\_entity\_ids) | n/a | `list(string)` | `null` | no |
| <a name="input_idp_metadata_table"></a> [idp\_metadata\_table](#input\_idp\_metadata\_table) | IDP Metadata table configurations. | <pre>object({<br/>    point_in_time_recovery_enabled = optional(bool, false)<br/>    stream_enabled                 = optional(bool, false)<br/>    stream_view_type               = optional(string, null)<br/>    deletion_protection_enabled    = optional(bool, false)<br/>    replication_regions = optional(list(object({<br/>      region_name            = string<br/>      propagate_tags         = optional(bool, true)<br/>      point_in_time_recovery = optional(bool, true)<br/>    })), [])<br/>  })</pre> | n/a | yes |
| <a name="input_idp_status_history_table"></a> [idp\_status\_history\_table](#input\_idp\_status\_history\_table) | IDP status history table configurations. | <pre>object({<br/>    point_in_time_recovery_enabled = optional(bool, false)<br/>    stream_enabled                 = optional(bool, false)<br/>    stream_view_type               = optional(string, null)<br/>    deletion_protection_enabled    = optional(bool, false)<br/>    replication_regions = optional(list(object({<br/>      region_name            = string<br/>      propagate_tags         = optional(bool, true)<br/>      point_in_time_recovery = optional(bool, true)<br/>    })), [])<br/>  })</pre> | n/a | yes |
| <a name="input_kms_rotation_period_in_days"></a> [kms\_rotation\_period\_in\_days](#input\_kms\_rotation\_period\_in\_days) | n/a | `number` | `365` | no |
| <a name="input_kms_ssm_enable_rotation"></a> [kms\_ssm\_enable\_rotation](#input\_kms\_ssm\_enable\_rotation) | n/a | `bool` | `true` | no |
| <a name="input_sessions_table"></a> [sessions\_table](#input\_sessions\_table) | Saml responses table configurations. | <pre>object({<br/>    ttl_enabled                    = optional(bool, true)<br/>    point_in_time_recovery_enabled = optional(bool, false)<br/>    stream_enabled                 = optional(bool, false)<br/>    stream_view_type               = optional(string, null)<br/>    deletion_protection_enabled    = optional(bool, false)<br/>  })</pre> | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_dynamodb_clients_table_stream_arn"></a> [dynamodb\_clients\_table\_stream\_arn](#output\_dynamodb\_clients\_table\_stream\_arn) | n/a |
| <a name="output_dynamodb_table_stream_arn"></a> [dynamodb\_table\_stream\_arn](#output\_dynamodb\_table\_stream\_arn) | n/a |
| <a name="output_kms_sessions_table_alias_arn"></a> [kms\_sessions\_table\_alias\_arn](#output\_kms\_sessions\_table\_alias\_arn) | n/a |
| <a name="output_table_client_registrations_arn"></a> [table\_client\_registrations\_arn](#output\_table\_client\_registrations\_arn) | n/a |
| <a name="output_table_client_registrations_name"></a> [table\_client\_registrations\_name](#output\_table\_client\_registrations\_name) | n/a |
| <a name="output_table_client_status_gsi_pointer_arn"></a> [table\_client\_status\_gsi\_pointer\_arn](#output\_table\_client\_status\_gsi\_pointer\_arn) | n/a |
| <a name="output_table_client_status_history_arn"></a> [table\_client\_status\_history\_arn](#output\_table\_client\_status\_history\_arn) | n/a |
| <a name="output_table_client_status_history_idx_name"></a> [table\_client\_status\_history\_idx\_name](#output\_table\_client\_status\_history\_idx\_name) | n/a |
| <a name="output_table_client_status_history_name"></a> [table\_client\_status\_history\_name](#output\_table\_client\_status\_history\_name) | n/a |
| <a name="output_table_idpMetadata_gsi_pointer_arn"></a> [table\_idpMetadata\_gsi\_pointer\_arn](#output\_table\_idpMetadata\_gsi\_pointer\_arn) | n/a |
| <a name="output_table_idp_metadata_arn"></a> [table\_idp\_metadata\_arn](#output\_table\_idp\_metadata\_arn) | n/a |
| <a name="output_table_idp_metadata_idx_name"></a> [table\_idp\_metadata\_idx\_name](#output\_table\_idp\_metadata\_idx\_name) | n/a |
| <a name="output_table_idp_metadata_name"></a> [table\_idp\_metadata\_name](#output\_table\_idp\_metadata\_name) | n/a |
| <a name="output_table_idp_status_gsi_pointer_arn"></a> [table\_idp\_status\_gsi\_pointer\_arn](#output\_table\_idp\_status\_gsi\_pointer\_arn) | n/a |
| <a name="output_table_idp_status_history_arn"></a> [table\_idp\_status\_history\_arn](#output\_table\_idp\_status\_history\_arn) | n/a |
| <a name="output_table_idp_status_history_idx_name"></a> [table\_idp\_status\_history\_idx\_name](#output\_table\_idp\_status\_history\_idx\_name) | n/a |
| <a name="output_table_idp_status_history_name"></a> [table\_idp\_status\_history\_name](#output\_table\_idp\_status\_history\_name) | n/a |
| <a name="output_table_sessions_arn"></a> [table\_sessions\_arn](#output\_table\_sessions\_arn) | n/a |
| <a name="output_table_sessions_gsi_code_arn"></a> [table\_sessions\_gsi\_code\_arn](#output\_table\_sessions\_gsi\_code\_arn) | n/a |
| <a name="output_table_sessions_name"></a> [table\_sessions\_name](#output\_table\_sessions\_name) | n/a |
<!-- END_TF_DOCS -->