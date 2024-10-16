## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38 |
| <a name="requirement_random"></a> [random](#requirement\_random) | 3.6.1 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 5.38 |
| <a name="provider_random"></a> [random](#provider\_random) | 3.6.1 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_kms_assertions_bucket"></a> [kms\_assertions\_bucket](#module\_kms\_assertions\_bucket) | terraform-aws-modules/kms/aws | 2.2.1 |
| <a name="module_s3_assertions_bucket"></a> [s3\_assertions\_bucket](#module\_s3\_assertions\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |
| <a name="module_s3_assets_bucket"></a> [s3\_assets\_bucket](#module\_s3\_assets\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |
| <a name="module_s3_athena_output_bucket"></a> [s3\_athena\_output\_bucket](#module\_s3\_athena\_output\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |

## Resources

| Name | Type |
|------|------|
| [aws_athena_database.assertions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/athena_database) | resource |
| [aws_athena_workgroup.assertions_workgroup](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/athena_workgroup) | resource |
| [aws_glue_catalog_database.assertions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/glue_catalog_database) | resource |
| [aws_glue_crawler.assertions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/glue_crawler) | resource |
| [aws_iam_policy.github_s3_policy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_policy.glue_assertions_policy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_role.githubS3deploy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role.glue_assertions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role_policy_attachment.deploy_s3](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.glue_s3_assertions_policy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy_attachment) | resource |
| [random_integer.assertion_bucket_suffix](https://registry.terraform.io/providers/hashicorp/random/3.6.1/docs/resources/integer) | resource |
| [random_integer.asset_bucket_suffix](https://registry.terraform.io/providers/hashicorp/random/3.6.1/docs/resources/integer) | resource |
| [aws_iam_policy_document.glue_assertions_policy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.glue_assume_role_policy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_account_id"></a> [account\_id](#input\_account\_id) | AWS Account id. | `string` | n/a | yes |
| <a name="input_assertion_bucket"></a> [assertion\_bucket](#input\_assertion\_bucket) | n/a | <pre>object({<br>    name_prefix                     = string<br>    glacier_transaction_days        = number<br>    expiration_days                 = number<br>    mfa_delete                      = optional(bool, false)<br>    kms_key_deletion_window_in_days = optional(number, 10)<br><br>    object_lock_legal_hold_status = optional(bool, false)<br>    object_lock_configuration     = optional(any, null)<br>    enable_key_rotation           = optional(bool, false)<br>  })</pre> | n/a | yes |
| <a name="input_assets_bucket_prefix"></a> [assets\_bucket\_prefix](#input\_assets\_bucket\_prefix) | n/a | `string` | n/a | yes |
| <a name="input_github_repository"></a> [github\_repository](#input\_github\_repository) | n/a | `string` | n/a | yes |
| <a name="input_assertions_crawler_schedule"></a> [assertions\_crawler\_schedule](#input\_assertions\_crawler\_schedule) | A cron expression used to specify the schedule | `string` | `null` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_assertions_bucket_arn"></a> [assertions\_bucket\_arn](#output\_assertions\_bucket\_arn) | n/a |
| <a name="output_assertions_bucket_name"></a> [assertions\_bucket\_name](#output\_assertions\_bucket\_name) | n/a |
| <a name="output_kms_assertion_key_arn"></a> [kms\_assertion\_key\_arn](#output\_kms\_assertion\_key\_arn) | n/a |

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.49 |
| <a name="requirement_random"></a> [random](#requirement\_random) | 3.6.1 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 5.49 |
| <a name="provider_random"></a> [random](#provider\_random) | 3.6.1 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_kms_assertions_bucket"></a> [kms\_assertions\_bucket](#module\_kms\_assertions\_bucket) | terraform-aws-modules/kms/aws | 3.0.0 |
| <a name="module_s3_assertions_bucket"></a> [s3\_assertions\_bucket](#module\_s3\_assertions\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |
| <a name="module_s3_assets_bucket"></a> [s3\_assets\_bucket](#module\_s3\_assets\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |
| <a name="module_s3_athena_output_bucket"></a> [s3\_athena\_output\_bucket](#module\_s3\_athena\_output\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |
| <a name="module_s3_idp_metadata_bucket"></a> [s3\_idp\_metadata\_bucket](#module\_s3\_idp\_metadata\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |

## Resources

| Name | Type |
|------|------|
| [aws_athena_database.assertions](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/athena_database) | resource |
| [aws_athena_workgroup.assertions_workgroup](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/athena_workgroup) | resource |
| [aws_glue_catalog_database.assertions](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/glue_catalog_database) | resource |
| [aws_glue_crawler.assertions](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/glue_crawler) | resource |
| [aws_iam_policy.github_s3_policy](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_policy) | resource |
| [aws_iam_policy.glue_assertions_policy](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_policy) | resource |
| [aws_iam_policy.replication](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_policy) | resource |
| [aws_iam_policy.upload_idp_policy](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_policy) | resource |
| [aws_iam_policy_attachment.replication](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_policy_attachment) | resource |
| [aws_iam_role.githubS3deploy](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_role) | resource |
| [aws_iam_role.glue_assertions](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_role) | resource |
| [aws_iam_role.replication](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_role) | resource |
| [aws_iam_role.upload_idp_role](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_role) | resource |
| [aws_iam_role_policy_attachment.deploy_s3](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.glue_s3_assertions_policy](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.upload_idp](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/resources/iam_role_policy_attachment) | resource |
| [random_integer.assertion_bucket_suffix](https://registry.terraform.io/providers/hashicorp/random/3.6.1/docs/resources/integer) | resource |
| [random_integer.asset_bucket_suffix](https://registry.terraform.io/providers/hashicorp/random/3.6.1/docs/resources/integer) | resource |
| [random_integer.idp_metadata_bucket_suffix](https://registry.terraform.io/providers/hashicorp/random/3.6.1/docs/resources/integer) | resource |
| [aws_iam_policy_document.glue_assertions_policy](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.glue_assume_role_policy](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.lambda_assertions](https://registry.terraform.io/providers/hashicorp/aws/5.49/docs/data-sources/iam_policy_document) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_account_id"></a> [account\_id](#input\_account\_id) | AWS Account id. | `string` | n/a | yes |
| <a name="input_assertion_bucket"></a> [assertion\_bucket](#input\_assertion\_bucket) | n/a | <pre>object({<br>    name_prefix                     = string<br>    expiration_days                 = number<br>    mfa_delete                      = optional(bool, false)<br>    kms_key_deletion_window_in_days = optional(number, 10)<br>    kms_multi_region                = optional(bool, false)<br><br>    object_lock_legal_hold_status = optional(bool, false)<br>    object_lock_configuration     = optional(any, null)<br>    enable_key_rotation           = optional(bool, false)<br>    replication_configuration = optional(<br>      object({<br>        id                     = string<br>        destination_bucket_arn = string<br>        kms_key_replica_arn    = string<br>    }), null)<br><br>    lambda_role_arn = optional(string, null)<br>  })</pre> | n/a | yes |
| <a name="input_assertions_crawler_schedule"></a> [assertions\_crawler\_schedule](#input\_assertions\_crawler\_schedule) | A cron expression used to specify the schedule | `string` | `null` | no |
| <a name="input_assets_bucket_prefix"></a> [assets\_bucket\_prefix](#input\_assets\_bucket\_prefix) | Assets bucket prefix | `string` | `""` | no |
| <a name="input_create_assets_bucket"></a> [create\_assets\_bucket](#input\_create\_assets\_bucket) | Creare assets bucket. | `bool` | `true` | no |
| <a name="input_create_athena_table"></a> [create\_athena\_table](#input\_create\_athena\_table) | Create athena table, query, glue crawler and all related resources. | `bool` | `true` | no |
| <a name="input_create_idp_metadata_bucket"></a> [create\_idp\_metadata\_bucket](#input\_create\_idp\_metadata\_bucket) | Create idp metadata bucket. | `bool` | `true` | no |
| <a name="input_github_repository"></a> [github\_repository](#input\_github\_repository) | n/a | `string` | n/a | yes |
| <a name="input_idp_metadata_bucket_prefix"></a> [idp\_metadata\_bucket\_prefix](#input\_idp\_metadata\_bucket\_prefix) | Idp metadata bucket prefix. | `string` | `""` | no |
| <a name="input_kms_rotation_period_in_days"></a> [kms\_rotation\_period\_in\_days](#input\_kms\_rotation\_period\_in\_days) | n/a | `number` | `365` | no |
| <a name="input_role_prefix"></a> [role\_prefix](#input\_role\_prefix) | Prefix to assign to the roles. | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_assertions_bucket_arn"></a> [assertions\_bucket\_arn](#output\_assertions\_bucket\_arn) | n/a |
| <a name="output_assertions_bucket_name"></a> [assertions\_bucket\_name](#output\_assertions\_bucket\_name) | n/a |
| <a name="output_assets_bucket_arn"></a> [assets\_bucket\_arn](#output\_assets\_bucket\_arn) | n/a |
| <a name="output_assets_bucket_name"></a> [assets\_bucket\_name](#output\_assets\_bucket\_name) | n/a |
| <a name="output_deploy_assets_role"></a> [deploy\_assets\_role](#output\_deploy\_assets\_role) | n/a |
| <a name="output_idp_metadata_bucket_arn"></a> [idp\_metadata\_bucket\_arn](#output\_idp\_metadata\_bucket\_arn) | n/a |
| <a name="output_kms_assertion_key_arn"></a> [kms\_assertion\_key\_arn](#output\_kms\_assertion\_key\_arn) | n/a |
| <a name="output_s3_idp_metadata_bucket_name"></a> [s3\_idp\_metadata\_bucket\_name](#output\_s3\_idp\_metadata\_bucket\_name) | n/a |
<!-- END_TF_DOCS -->