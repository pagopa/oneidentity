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
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38 |

## Providers

No providers.

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_dynamodb_sessions_table"></a> [dynamodb\_sessions\_table](#module\_dynamodb\_sessions\_table) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_dynamodb_table_client_registrations"></a> [dynamodb\_table\_client\_registrations](#module\_dynamodb\_table\_client\_registrations) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_dynamodb_table_idpMetadata"></a> [dynamodb\_table\_idpMetadata](#module\_dynamodb\_table\_idpMetadata) | terraform-aws-modules/dynamodb-table/aws | 4.0.1 |
| <a name="module_kms_sessions_table"></a> [kms\_sessions\_table](#module\_kms\_sessions\_table) | terraform-aws-modules/kms/aws | 2.2.1 |

## Resources

No resources.

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_client_registrations_table"></a> [client\_registrations\_table](#input\_client\_registrations\_table) | Client registrations table configurations. | <pre>object({<br>    point_in_time_recovery_enabled = optional(bool, false)<br>    stream_enabled                 = optional(bool, false)<br>    stream_view_type               = optional(string, null)<br>    replication_regions = optional(list(object({<br>      region_name            = string<br>      propagate_tags         = optional(bool, true)<br>      point_in_time_recovery = optional(bool, true)<br>    })), [])<br>  })</pre> | n/a | yes |
| <a name="input_idp_metadata_table"></a> [idp\_metadata\_table](#input\_idp\_metadata\_table) | IDP Metadata table configurations. | <pre>object({<br>    point_in_time_recovery_enabled = optional(bool, false)<br>    stream_enabled                 = optional(bool, false)<br>    stream_view_type               = optional(string, null)<br>    replication_regions = optional(list(object({<br>      region_name            = string<br>      propagate_tags         = optional(bool, true)<br>      point_in_time_recovery = optional(bool, true)<br>    })), [])<br>  })</pre> | n/a | yes |
| <a name="input_sessions_table"></a> [sessions\_table](#input\_sessions\_table) | Saml responses table configurations. | <pre>object({<br>    ttl_enabled                    = optional(bool, true)<br>    point_in_time_recovery_enabled = optional(bool, false)<br>    stream_enabled                 = optional(bool, false)<br>    stream_view_type               = optional(string, null)<br>  })</pre> | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_dynamodb_table_stream_arn"></a> [dynamodb\_table\_stream\_arn](#output\_dynamodb\_table\_stream\_arn) | n/a |
| <a name="output_kms_sessions_table_alias_arn"></a> [kms\_sessions\_table\_alias\_arn](#output\_kms\_sessions\_table\_alias\_arn) | n/a |
| <a name="output_table_client_registrations_arn"></a> [table\_client\_registrations\_arn](#output\_table\_client\_registrations\_arn) | n/a |
| <a name="output_table_client_registrations_name"></a> [table\_client\_registrations\_name](#output\_table\_client\_registrations\_name) | n/a |
| <a name="output_table_idpMetadata_gsi_pointer_arn"></a> [table\_idpMetadata\_gsi\_pointer\_arn](#output\_table\_idpMetadata\_gsi\_pointer\_arn) | n/a |
| <a name="output_table_idp_metadata_arn"></a> [table\_idp\_metadata\_arn](#output\_table\_idp\_metadata\_arn) | n/a |
| <a name="output_table_idp_metadata_idx_name"></a> [table\_idp\_metadata\_idx\_name](#output\_table\_idp\_metadata\_idx\_name) | n/a |
| <a name="output_table_idp_metadata_name"></a> [table\_idp\_metadata\_name](#output\_table\_idp\_metadata\_name) | n/a |
| <a name="output_table_sessions_arn"></a> [table\_sessions\_arn](#output\_table\_sessions\_arn) | n/a |
| <a name="output_table_sessions_gsi_code_arn"></a> [table\_sessions\_gsi\_code\_arn](#output\_table\_sessions\_gsi\_code\_arn) | n/a |
| <a name="output_table_sessions_name"></a> [table\_sessions\_name](#output\_table\_sessions\_name) | n/a |
<!-- END_TF_DOCS -->