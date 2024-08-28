<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | 5.38 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 5.38 |

## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [aws_sns_topic.alarms](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/sns_topic) | resource |
| [aws_sns_topic_subscription.alarms_email](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/resources/sns_topic_subscription) | resource |
| [aws_ssm_parameter.subscribers](https://registry.terraform.io/providers/hashicorp/aws/5.38/docs/data-sources/ssm_parameter) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_sns_topic_name"></a> [sns\_topic\_name](#input\_sns\_topic\_name) | n/a | `string` | n/a | yes |
| <a name="input_ssm_parameter_name"></a> [ssm\_parameter\_name](#input\_ssm\_parameter\_name) | n/a | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_sns_topic_arn"></a> [sns\_topic\_arn](#output\_sns\_topic\_arn) | n/a |
<!-- END_TF_DOCS -->