module "kms_table_saml_responses" {
  source  = "terraform-aws-modules/kms/aws"
  version = "2.2.1"

  description = "KMS key for Dynamodb table encryption."
  key_usage   = "ENCRYPT_DECRYPT"

  # Aliases
  aliases = ["saml_responses/dynamodb"]
}



module "dynamodb_table_saml_responses" {
  source  = "terraform-aws-modules/dynamodb-table/aws"
  version = "4.0.1"

  name = var.saml_responses_table.name

  hash_key = "accessToken"

  attributes = [
    {
      name = "accessToken"
      type = "S"
    },
    /*
    {
      name = "samlResponse"
      type = "S"
    },
    {
      name = "expirationTime"
      type = "N"
    }
    */
  ]

  ttl_attribute_name = "expirationTime"
  ttl_enabled        = var.saml_responses_table.ttl_enabled

  billing_mode = "PAY_PER_REQUEST"

  point_in_time_recovery_enabled = var.saml_responses_table.point_in_time_recovery_enabled

  server_side_encryption_enabled     = true
  server_side_encryption_kms_key_arn = module.kms_table_saml_responses.aliases["saml_responses/dynamodb"].arn

  tags = {
    Name = var.saml_responses_table.name
  }

}
