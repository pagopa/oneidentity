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
| <a name="module_s3_assetion_bucket"></a> [s3\_assetion\_bucket](#module\_s3\_assetion\_bucket) | terraform-aws-modules/s3-bucket/aws | 4.1.1 |

## Resources

| Name | Type |
|------|------|
| [random_integer.assetion_bucket_suffix](https://registry.terraform.io/providers/haschicorp/random/3.6.1/docs/resources/integer) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_assertion_bucket"></a> [assertion\_bucket](#input\_assertion\_bucket) | n/a | <pre>object({<br>    name_prefix              = string<br>    gracier_transaction_days = number<br>    expiration_days          = number<br>    mfa_delete               = optional(bool, false)<br>  })</pre> | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_s3_assetion_bucket"></a> [s3\_assetion\_bucket](#output\_s3\_assetion\_bucket) | n/a |
