resource "random_integer" "assertion_bucket_suffix" {
  min = 1000
  max = 9999
}

locals {
  bucket_name = format("%s-%s", var.assertion_bucket.name_prefix,
    random_integer.assertion_bucket_suffix.result
  )
  athena_outputs = format("query-%s", local.bucket_name)

}

module "kms_assertions_bucket" {
  source  = "terraform-aws-modules/kms/aws"
  version = "2.2.1"

  description         = "KMS key for S3 encryption"
  key_usage           = "ENCRYPT_DECRYPT"
  enable_key_rotation = var.assertion_bucket.enable_key_rotation

  # Aliases
  aliases = ["assertions/S3"]
}


module "s3_assertions_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.bucket_name
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  server_side_encryption_configuration = {
    rule = {
      bucket_key_enabled = true
      apply_server_side_encryption_by_default = {
        kms_master_key_id = module.kms_assertions_bucket.aliases["assertions/S3"].arn
        sse_algorithm     = "aws:kms"
      }
    }
  }

  versioning = {
    enabled    = true
    mfa_delete = var.assertion_bucket.mfa_delete
  }

  lifecycle_rule = [
    {
      enabled = true
      id      = "glacier_rule"
      prefix  = ""
      tags    = {}

      transition = [
        {
          days          = var.assertion_bucket.glacier_transaction_days
          storage_class = "GLACIER"
        }
      ]

      expiration = {
        days = var.assertion_bucket.expiration_days
      }
    }
  ]

  tags = {
    Name = local.bucket_name
  }
}

## Athena ##

module "s3_athena_output_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.1.1"

  bucket = local.athena_outputs
  acl    = "private"

  control_object_ownership = true
  object_ownership         = "ObjectWriter"

  server_side_encryption_configuration = {
    rule = {
      bucket_key_enabled = true
      apply_server_side_encryption_by_default = {
        kms_master_key_id = module.kms_assertions_bucket.aliases["assertions/S3"].arn
        sse_algorithm     = "aws:kms"
      }
    }
  }

  tags = {
    Name = local.bucket_name
  }
}

resource "aws_athena_workgroup" "assertions_workgroup" {
  name = "assertions_workgroup"

  configuration {
    result_configuration {
      output_location = "s3://${local.athena_outputs}/output/"
    }
  }
}

# Create Athena database
resource "aws_athena_database" "assertions" {
  name   = "assertions"
  bucket = module.s3_athena_output_bucket.s3_bucket_id
}

# Create Athena table
resource "aws_athena_named_query" "create_assertions_table" {
  name     = "create_assertions_table"
  database = aws_athena_database.assertions.name
  workgroup = aws_athena_workgroup.assertions_workgroup.id
  query    = <<EOF
CREATE EXTERNAL TABLE `${module.s3_assertions_bucket.s3_bucket_id}`(
  `samlrequestid` string COMMENT 'from deserializer', 
  `recordtype` string COMMENT 'from deserializer', 
  `creationtime` string COMMENT 'from deserializer', 
  `clientid` string COMMENT 'from deserializer', 
  `idp` string COMMENT 'from deserializer', 
  `nonce` string COMMENT 'from deserializer', 
  `redirecturi` string COMMENT 'from deserializer', 
  `responsetype` string COMMENT 'from deserializer', 
  `samlrequest` string COMMENT 'from deserializer', 
  `samlresponse` string COMMENT 'from deserializer', 
  `scope` string COMMENT 'from deserializer', 
  `state` string COMMENT 'from deserializer', 
  `ttl` string COMMENT 'from deserializer', 
  `code` string COMMENT 'from deserializer', 
  `idtoken` string COMMENT 'from deserializer', 
  `eventname` string COMMENT 'from deserializer')
PARTITIONED BY ( 
  `partition_0` string, 
  `partition_1` string, 
  `partition_2` string, 
  `partition_3` string, 
  `partition_4` string, 
  `partition_5` string)
ROW FORMAT SERDE 
  'org.openx.data.jsonserde.JsonSerDe' 
WITH SERDEPROPERTIES ( 
  'paths'='SAMLRequest,SAMLResponse,clientId,code,creationTime,eventName,idToken,idp,nonce,recordType,redirectUri,responseType,samlRequestID,scope,state,ttl') 
STORED AS INPUTFORMAT 
  'org.apache.hadoop.mapred.TextInputFormat' 
OUTPUTFORMAT 
  'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION
  's3://${module.s3_assertions_bucket.s3_bucket_id}/'
TBLPROPERTIES (
  'classification'='json', 
  'compressionType'='none', 
  'partition_filtering.enabled'='true',
  'typeOfData'='file')
EOF
}