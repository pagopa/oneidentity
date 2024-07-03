## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38 |
| <a name="requirement_random"></a> [random](#requirement\_random) | 3.6.1 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_random"></a> [random](#provider\_random) | 3.6.1 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_kms_assertions_bucket"></a> [kms\_assertions\_bucket](#module\_kms\_assertions\_bucket) | terraform-aws-modules/kms/aws | 2.2.1 |
| <a name="module_s3_assertions_bucket"></a> [s3\_assertions\_bucket](#module\_s3\_assertions\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |

## Resources

| Name | Type |
|------|------|
| [random_integer.assertion_bucket_suffix](https://registry.terraform.io/providers/hashicorp/random/3.6.1/docs/resources/integer) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_assertion_bucket"></a> [assertion\_bucket](#input\_assertion\_bucket) | n/a | <pre>object({<br>    name_prefix                     = string<br>    glacier_transaction_days        = number<br>    expiration_days                 = number<br>    mfa_delete                      = optional(bool, false)<br>    kms_key_deletion_window_in_days = optional(number, 10)<br><br>    object_lock_legal_hold_status = optional(bool, false)<br>    object_lock_configuration     = optional(any, null)<br>    enable_key_rotation           = optional(bool, false)<br>  })</pre> | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_assertions_bucket_arn"></a> [assertions\_bucket\_arn](#output\_assertions\_bucket\_arn) | n/a |
| <a name="output_assertions_bucket_name"></a> [assertions\_bucket\_name](#output\_assertions\_bucket\_name) | n/a |
