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
| <a name="module_s3_athena_output_bucket"></a> [s3\_athena\_output\_bucket](#module\_s3\_athena\_output\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |

## Resources

| Name | Type |
|------|------|
| [aws_athena_database.assertions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/athena_database) | resource |
| [aws_athena_workgroup.assertions_workgroup](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/athena_workgroup) | resource |
| [aws_glue_catalog_database.assertions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/glue_catalog_database) | resource |
| [aws_glue_crawler.assertions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/glue_crawler) | resource |
| [aws_iam_policy.glue_assertions_policy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_policy) | resource |
| [aws_iam_role.glue_assertions](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role) | resource |
| [aws_iam_role_policy_attachment.glue_s3_assertions_policy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/iam_role_policy_attachment) | resource |
| [random_integer.assertion_bucket_suffix](https://registry.terraform.io/providers/hashicorp/random/3.6.1/docs/resources/integer) | resource |
| [aws_iam_policy_document.glue_assertions_policy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.glue_assume_role_policy](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/iam_policy_document) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_assertion_bucket"></a> [assertion\_bucket](#input\_assertion\_bucket) | n/a | <pre>object({<br>    name_prefix                     = string<br>    glacier_transaction_days        = number<br>    expiration_days                 = number<br>    mfa_delete                      = optional(bool, false)<br>    kms_key_deletion_window_in_days = optional(number, 10)<br><br>    object_lock_legal_hold_status = optional(bool, false)<br>    object_lock_configuration     = optional(any, null)<br>    enable_key_rotation           = optional(bool, false)<br>  })</pre> | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_assertions_bucket_arn"></a> [assertions\_bucket\_arn](#output\_assertions\_bucket\_arn) | n/a |
| <a name="output_assertions_bucket_name"></a> [assertions\_bucket\_name](#output\_assertions\_bucket\_name) | n/a |
| <a name="output_kms_assertion_key_arn"></a> [kms\_assertion\_key\_arn](#output\_kms\_assertion\_key\_arn) | n/a |
