<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | 1.7.4 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38.0 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 5.38.0 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_storage"></a> [storage](#module\_storage) | ../../modules/storage | n/a |

## Resources

| Name | Type |
|------|------|
| [aws_caller_identity.current](https://registry.terraform.io/providers/hashicorp/aws/5.38.0/docs/data-sources/caller_identity) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | App name. | `string` | `"oneid"` | no |
| <a name="input_assertion_bucket"></a> [assertion\_bucket](#input\_assertion\_bucket) | Assertion storage. | <pre>object({<br>    mfa_delete                = bool<br>    glacier_transaction_days  = number<br>    expiration_days           = number<br>    kms_multi_region          = bool<br>    enable_key_rotation       = bool<br>    object_lock_configuration = any<br>    replication_configuration = optional(<br>      object({<br>        id                     = string<br>        destination_bucket_arn = string<br>        kms_key_replica_arn    = string<br>    }), null)<br>    lambda_role_arn = optional(string, null)<br><br>  })</pre> | <pre>{<br>  "enable_key_rotation": true,<br>  "expiration_days": 731,<br>  "glacier_transaction_days": 90,<br>  "kms_multi_region": false,<br>  "lambda_role_arn": "arn:aws:iam::851725347804:role/oneid-es-1-p-assertion",<br>  "mfa_delete": false,<br>  "object_lock_configuration": {<br>    "rule": {<br>      "default_retention": {<br>        "days": 730,<br>        "mode": "GOVERNANCE"<br>      }<br>    }<br>  }<br>}</pre> | no |
| <a name="input_assertions_crawler_schedule"></a> [assertions\_crawler\_schedule](#input\_assertions\_crawler\_schedule) | A cron expression used to specify the schedule | `string` | `"cron(00 08 ? * MON *)"` | no |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS region to create resources. Default Milan | `string` | `"eu-south-1"` | no |
| <a name="input_aws_region_short"></a> [aws\_region\_short](#input\_aws\_region\_short) | AWS region short format. | `string` | `"es-1"` | no |
| <a name="input_env_short"></a> [env\_short](#input\_env\_short) | Environment short. | `string` | `"p"` | no |
| <a name="input_tags"></a> [tags](#input\_tags) | n/a | `map(any)` | <pre>{<br>  "CostCenter": "tier0",<br>  "CreatedBy": "Terraform",<br>  "Environment": "Prod",<br>  "Owner": "Oneidentity",<br>  "Source": "https://github.com/pagopa/oneidentity"<br>}</pre> | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_assertions_bucket_arn"></a> [assertions\_bucket\_arn](#output\_assertions\_bucket\_arn) | n/a |
| <a name="output_assertions_bucket_name"></a> [assertions\_bucket\_name](#output\_assertions\_bucket\_name) | n/a |
<!-- END_TF_DOCS -->