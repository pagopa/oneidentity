
# Backup role.
resource "aws_iam_role" "backup" {
  name = "${var.prefix}-aws-backup-role" # Replace with your desired role name

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "backup.amazonaws.com"
        }
      }
    ]
  })
}


# Required policy to backup the S3 objects.
resource "aws_iam_policy" "backup" {
  name        = "${var.prefix}-aws-backup-policy" # Replace with your desired policy name
  description = "Policy for AWS Backup to access S3 bucket"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "s3:Get*",
          "s3:PutBucketNotification",
          "s3:PutObject",
          "s3:ListBucket*"
        ],
        Effect = "Allow"
        Resource = [
          "arn:aws:s3:::*"
        ]
      },
      {
        Action = [
          "events:DescribeRule",
          "events:EnableRule",
          "events:PutRule",
          "events:DeleteRule",
          "events:PutTargets",
          "events:RemoveTargets",
          "events:ListTargetsByRule",
          "events:DisableRule"
        ],
        Effect   = "Allow"
        Resource = "arn:aws:events:*:*:rule/AwsBackupManagedRule*"
      },
      {
        Action   = "tag:GetResources"
        Effect   = "Allow"
        Resource = "*"
      },
      {
        Action = [
          "kms:Decrypt",
          "kms:DescribeKey"
        ],
        Effect   = "Allow"
        Resource = "*"
        Condition = {
          StringLike = {
            "kms:ViaService" : "s3.*.amazonaws.com"
          }
        }
      },
      {
        Action = [
          "cloudwatch:GetMetricData",
          "events:ListRules"
        ],
        Effect   = "Allow"
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy" "backup" {
  name   = "${var.prefix}-aws-backup-role-policy" # Replace with your desired role policy name
  role   = aws_iam_role.backup.name
  policy = aws_iam_policy.backup.policy
}


module "aws_backup" {
  source       = "git::https://github.com/pagopa/terraform-aws-backup.git?ref=v1.1.0"
  name         = "s3-backup"
  iam_role_arn = aws_iam_role.backup.arn

  selection_tag = {
    key   = "Backup"
    value = "True"
  }

  enable_vault_lock_governance = false

  backup_rule = [{
    rule_name = "backup_daily_rule"
    //schedule          = "cron(0 14 * * ? *)"
    start_window      = 60
    completion_window = 140
    lifecycle = {
      delete_after = 14
    }
    },
    {
      rule_name = "backup_default_rule"
    }
  ]
}