module "ecr" {
  source  = "terraform-aws-modules/ecr/aws"
  version = "1.6.0"

  for_each = { for r in var.ecr_registers : r.name => r }

  repository_name = each.key

  repository_read_write_access_arns = []

  repository_image_tag_mutability = each.value.repository_image_tag_mutability

  repository_lifecycle_policy = jsonencode({
    rules = [
      {
        rulePriority = 1,
        description  = "Keep last ${each.value.number_of_images_to_keep} images",
        selection = {
          "tagStatus" : "any",
          "countType" : "imageCountMoreThan",
          "countNumber" : each.value.number_of_images_to_keep
        },
        action = {
          type = "expire"
        }
      }
    ]
  })

  # Registry Replication Configuration
  # TODO: in production it might be replicated in another region.
  create_registry_replication_configuration = false
  registry_replication_rules                = []
}

# SSM parameters

data "aws_ssm_parameter" "certificate" {
  name = var.ssm_cert_key.cert_pem
}

data "aws_ssm_parameter" "key" {
  name = var.ssm_cert_key.key_pem
}

data "aws_ssm_parameter" "internal_idp_certificate" {
  count = var.internal_idp_enabled ? 1 : 0
  name  = var.ssm_idp_internal_cert_key.cert_pem
}

data "aws_ssm_parameter" "internal_idp_key" {
  count = var.internal_idp_enabled ? 1 : 0
  name  = var.ssm_idp_internal_cert_key.key_pem
}

module "kms_key_pem" {
  source  = "terraform-aws-modules/kms/aws"
  version = "3.0.0"

  description             = "KMS key for SSM parameter encryption"
  key_usage               = "ENCRYPT_DECRYPT"
  enable_key_rotation     = var.kms_ssm_enable_rotation
  enable_default_policy   = true
  rotation_period_in_days = var.kms_rotation_period_in_days

  # Aliases
  aliases = ["keyPem/SSM"]
}


resource "aws_ssm_parameter" "key_pem" {
  name   = var.ssm_cert_key.key_pem
  type   = "SecureString"
  value  = ""
  key_id = module.kms_key_pem.aliases["keyPem/SSM"].target_key_arn
  lifecycle {
    ignore_changes = [value]
  }
}

## KMS key to sign the Jwt tokens.
module "jwt_sign" {
  source  = "terraform-aws-modules/kms/aws"
  version = "2.2.1"

  description              = "KMS key to sign Jwt tokens"
  key_usage                = "SIGN_VERIFY"
  customer_master_key_spec = "RSA_2048"
  enable_key_rotation      = false


  # Aliases
  aliases = ["sign-jwt"]
}

resource "aws_iam_policy" "ecs_core_task" {
  name = format("%s-task-policy", var.service_core.service_name)
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "DynamoDBSessionsRW"
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
        ]
        Resource = [
          "${var.dynamodb_table_sessions.table_arn}"
        ]
      },
      {
        Sid = "DynamoDBGSISessionsR"
        Action = [
          "dynamodb:Query",
        ]
        Effect = "Allow"
        Resource = [
          "${var.dynamodb_table_sessions.gsi_code_arn}",
        ]
      },
      {
        Sid = "DynamoDBGSIIdpMetadata"
        Action = [
          "dynamodb:Query",
        ]
        Effect = "Allow"
        Resource = [
          "${var.dynamodb_table_idpMetadata.gsi_pointer_arn}",
        ]
      },
      {
        Sid = "DynamoDBIdpMetadataR"
        Action = [
          "dynamodb:GetItem",
        ]
        Effect = "Allow"
        Resource = [
          "${var.dynamodb_table_idpMetadata.table_arn}",
        ]
      },
      {
        Sid    = "DynamoDBClientRegistrationsReadOnly"
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:Scan",
        ]
        Resource = [
          "${var.table_client_registrations_arn}"
        ]
      },
      {
        Sid    = "DynamoDBLastIDPUsedRW"
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
        ]
        Resource = [
          "${var.table_last_idp_used_arn}"
        ]
      },
      {
        Sid    = "KSMSign"
        Effect = "Allow"
        Action = [
          "kms:Sign",
          "kms:GetPublicKey",
        ]
        Resource = [
          "arn:aws:kms:${var.aws_region}:${var.account_id}:key/*"
        ],
        Condition = {
          StringEquals = {
            "kms:RequestAlias" = "${module.jwt_sign.aliases.sign-jwt.name}"
          }
        }
      },
      {
        Sid    = "KMSDecryptEncryptSessions"
        Effect = "Allow"
        Action = [
          "kms:Decrypt",
          "kms:Encrypt",
        ]
        Resource = [
          var.kms_sessions_table_alias_arn
        ]
      },
      {
        Sid    = "KMSDecryptEncryptParameter"
        Effect = "Allow"
        Action = [
          "kms:Decrypt",
          "kms:Encrypt",
        ]
        Resource = [
          "${module.kms_key_pem.aliases["keyPem/SSM"].target_key_arn}"
        ]
      },
      {
        "Sid" : "SSMGetCertParameters",
        "Effect" : "Allow",
        "Action" : [
          "ssm:Describe*",
          "ssm:Get*",
          "ssm:List*"
        ],
        "Resource" : [
          "${data.aws_ssm_parameter.certificate.arn}",
          "${aws_ssm_parameter.key_pem.arn}",
          "arn:aws:ssm:${var.aws_region}:${var.account_id}:parameter/pdv/*"
        ]
      },
      {
        "Sid" : "CloudWatchPutCustomMetrics",
        "Effect" : "Allow",
        "Action" : [
          "cloudwatch:PutMetricData"
        ],
        "Resource" : [
          "*"
        ]
      },
      {
        "Sid" : "SQSSendMessage",
        "Effect" : "Allow",
        "Action" : [
          "sqs:SendMessage"
        ],
        "Resource" : [
          "${var.pdv_reconciler_lambda.pdv_errors_queue_arn}"
        ]
      },
    ]
  })

}


resource "aws_iam_policy" "ecs_internal_idp_task" {
  count = var.internal_idp_enabled ? 1 : 0
  name  = format("%s-task-policy", var.service_internal_idp.service_name)
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "DynamoDBIdPSessionRW"
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
        ]
        Resource = [
          "${var.dynamodb_table_internal_idp_session_arn}"
        ]
      },
      {
        Sid    = "DynamoDBIdPUsersRW"
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
        ]
        Resource = [
          "${var.dynamodb_table_internal_idp_users_arn}"
        ]
      },
      {
        Sid    = "DynamoDBClientRegistrationsReadOnly"
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:Scan",
        ]
        Resource = [
          "${var.table_client_registrations_arn}"
        ]
      },
      {
        Sid    = "KMSDecryptEncryptParameter"
        Effect = "Allow"
        Action = [
          "kms:Decrypt",
          "kms:Encrypt",
        ]
        Resource = [
          "${module.kms_key_pem.aliases["keyPem/SSM"].target_key_arn}"
        ]
      },
      {
        "Sid" : "SSMGetCertParameters",
        "Effect" : "Allow",
        "Action" : [
          "ssm:Describe*",
          "ssm:Get*",
          "ssm:List*"
        ],
        "Resource" : [
          "${data.aws_ssm_parameter.certificate.arn}",
          "${aws_ssm_parameter.key_pem.arn}",
          "${data.aws_ssm_parameter.internal_idp_certificate[0].arn}",
          "${data.aws_ssm_parameter.internal_idp_key[0].arn}"
        ]
      },
    ]
  })

}

module "ecs_cluster" {

  source  = "terraform-aws-modules/ecs/aws"
  version = "5.9.1"

  cluster_name = var.ecs_cluster_name

  cluster_settings = [
    {
      name  = "containerInsights"
      value = var.enable_container_insights ? "enabled" : "disabled"
    }
  ]

  # Capacity provider
  fargate_capacity_providers = var.fargate_capacity_providers
}

## Log group for ECS Core
resource "aws_cloudwatch_log_group" "ecs_core" {
  name = format("/aws/ecs/%s/%s", var.service_core.service_name, var.service_core.container.name)

  retention_in_days = var.service_core.container.logs_retention_days
}

## ECS Core
module "ecs_core_service" {
  source  = "terraform-aws-modules/ecs/aws//modules/service"
  version = "5.9.1"

  name = var.service_core.service_name

  cluster_arn = module.ecs_cluster.cluster_arn

  cpu    = var.service_core.cpu
  memory = var.service_core.memory

  enable_execute_command = var.service_core.enable_execute_command

  tasks_iam_role_policies = {
    ecs_core_task = aws_iam_policy.ecs_core_task.arn
  }


  container_definitions = {
    "${var.service_core.container.name}" = {
      cpu                         = var.service_core.container.cpu
      memory                      = var.service_core.memory
      create_cloudwatch_log_group = false

      essential = true
      image     = "${module.ecr[var.service_core.container.image_name].repository_url}:${var.service_core.container.image_version}",

      port_mappings = [
        {
          name          = var.service_core.container.name
          containerPort = var.service_core.container.containerPort
          hostPort      = var.service_core.container.hostPort
          protocol      = "tcp"
        }
      ]

      log_configuration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.ecs_core.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
          mode                  = "non-blocking"
        }
      }

      environment = setunion(var.service_core.environment_variables, [
        {
          name  = "SIGN_JWT_KEY_ALIAS"
          value = module.jwt_sign.aliases.sign-jwt.name
        }
      ])

      readonly_root_filesystem = false
    }
  }

  enable_autoscaling       = var.service_core.autoscaling.enable
  autoscaling_min_capacity = var.service_core.autoscaling.min_capacity
  autoscaling_max_capacity = var.service_core.autoscaling.max_capacity
  desired_count            = var.service_core.autoscaling.desired_count

  autoscaling_policies = {
    "cpu" : {
      "policy_type" : "TargetTrackingScaling",
      "target_tracking_scaling_policy_configuration" : {
        "predefined_metric_specification" : {
          "predefined_metric_type" : "ECSServiceAverageCPUUtilization"
        }
        "disable_scale_in" : true
      }
    },
    "memory" : {
      "policy_type" : "TargetTrackingScaling",
      "target_tracking_scaling_policy_configuration" : {
        "predefined_metric_specification" : {
          "predefined_metric_type" : "ECSServiceAverageMemoryUtilization"
        }
        "disable_scale_in" : true
      }
    },
    "cpu_high" : {
      "policy_type" : "StepScaling"
      "step_scaling_policy_configuration" : {
        "adjustment_type" : "ChangeInCapacity"
        "step_adjustment" : [
          {
            "scaling_adjustment" : var.service_core.cpu_high_scaling_adjustment # Add 10 tasks
            metric_interval_lower_bound = 0
          }
        ]
        cooldown = 60
      }
    },
    "cpu_low" : {
      "policy_type" : "StepScaling"
      "step_scaling_policy_configuration" : {
        "adjustment_type" : "ChangeInCapacity"
        "step_adjustment" : [
          {
            "scaling_adjustment" : -1 # Remove 1 task
            metric_interval_lower_bound = 0
          }
        ]
        cooldown = 300
      }
    }
  }


  subnet_ids       = var.private_subnets
  assign_public_ip = false

  load_balancer = {
    service = {
      target_group_arn = module.elb.target_groups["ecs-oneid-core"].arn
      container_name   = var.service_core.container.name
      container_port   = var.service_core.container.containerPort
    }
  }

  security_group_rules = {
    alb_ingress_3000 = {
      type                     = "ingress"
      from_port                = var.service_core.container.containerPort
      to_port                  = var.service_core.container.containerPort
      protocol                 = "tcp"
      description              = "Service port"
      source_security_group_id = module.elb.security_group_id
    }
    egress_all = {
      type        = "egress"
      from_port   = 0
      to_port     = 0
      protocol    = "-1"
      cidr_blocks = ["0.0.0.0/0"]
    }
  }

}

## Log group for ECS Internal IDP
resource "aws_cloudwatch_log_group" "ecs_internal_idp" {
  count = var.internal_idp_enabled ? 1 : 0
  name  = format("/aws/ecs/%s/%s", var.service_internal_idp.service_name, var.service_internal_idp.container.name)

  retention_in_days = var.service_internal_idp.container.logs_retention_days
}

## ECS Internal IDP
module "ecs_internal_idp_service" {
  count   = var.internal_idp_enabled ? 1 : 0
  source  = "terraform-aws-modules/ecs/aws//modules/service"
  version = "5.9.1"

  name = var.service_internal_idp.service_name

  cluster_arn = module.ecs_cluster.cluster_arn

  cpu    = var.service_internal_idp.cpu
  memory = var.service_internal_idp.memory

  enable_execute_command = var.service_internal_idp.enable_execute_command

  tasks_iam_role_policies = {
    ecs_internal_idp_task = aws_iam_policy.ecs_internal_idp_task[0].arn
  }


  container_definitions = {
    (var.service_internal_idp.container.name) = {
      cpu                         = var.service_internal_idp.container.cpu
      memory                      = var.service_internal_idp.memory
      create_cloudwatch_log_group = false

      essential = true
      image     = "${module.ecr[var.service_internal_idp.container.image_name].repository_url}:${var.service_internal_idp.container.image_version}",

      port_mappings = [
        {
          name          = var.service_internal_idp.container.name
          containerPort = var.service_internal_idp.container.containerPort
          hostPort      = var.service_internal_idp.container.hostPort
          protocol      = "tcp"
        }
      ]

      log_configuration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.ecs_internal_idp[0].name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
          mode                  = "non-blocking"
        }
      }

      environment = var.service_internal_idp.environment_variables

      readonly_root_filesystem = false
    }
  }

  enable_autoscaling       = var.service_internal_idp.autoscaling.enable
  autoscaling_min_capacity = var.service_internal_idp.autoscaling.min_capacity
  autoscaling_max_capacity = var.service_internal_idp.autoscaling.max_capacity
  desired_count            = var.service_internal_idp.autoscaling.desired_count


  subnet_ids       = var.private_subnets
  assign_public_ip = false

  load_balancer = {
    service = {
      target_group_arn = module.elb.target_groups["ecs-oneid-internal-idp"].arn
      container_name   = var.service_internal_idp.container.name
      container_port   = var.service_internal_idp.container.containerPort
    }
  }

  security_group_rules = {
    alb_ingress_3000 = {
      type                     = "ingress"
      from_port                = var.service_internal_idp.container.containerPort
      to_port                  = var.service_internal_idp.container.containerPort
      protocol                 = "tcp"
      description              = "Service port"
      source_security_group_id = module.elb.security_group_id
    }
    egress_all = {
      type        = "egress"
      from_port   = 0
      to_port     = 0
      protocol    = "-1"
      cidr_blocks = ["0.0.0.0/0"]
    }
  }

}

resource "aws_cloudwatch_metric_alarm" "ecs_alarms" {
  for_each            = var.ecs_alarms
  alarm_name          = format("%s-%s", module.ecs_core_service.name, each.key)
  comparison_operator = each.value.comparison_operator
  evaluation_periods  = each.value.evaluation_periods
  metric_name         = each.value.metric_name
  namespace           = each.value.namespace
  period              = each.value.period
  statistic           = each.value.statistic
  threshold           = each.value.threshold


  dimensions = {
    ClusterName = module.ecs_cluster.cluster_name
    ServiceName = module.ecs_core_service.name
  }

  alarm_actions = compact([
    each.value.sns_topic_alarm_arn,
    each.value.scaling_policy != null ?
    module.ecs_core_service.autoscaling_policies[each.value.scaling_policy].arn : null,
  ])
}

resource "aws_cloudwatch_metric_alarm" "idp_error_alarm" {
  for_each            = var.idp_alarm != null ? { for s in var.idp_alarm.entity_id : s => s } : {}
  alarm_name          = format("%s_%s_%s", "IDPErrorRateAlarm", var.env_short, each.key)
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  threshold           = 1
  ok_actions          = [module.update_status_lambda.lambda_function_arn]


  alarm_actions = [
    var.sns_topic_arn,
    module.update_status_lambda.lambda_function_arn
  ]

  metric_query {
    id          = "error_rate"
    expression  = "errors/(successes+errors)*100"
    label       = "Error Rate"
    return_data = "true"
  }

  metric_query {
    id = "successes"

    metric {
      metric_name = "IDPSuccess"
      namespace   = var.idp_alarm.namespace
      period      = 60
      stat        = "Sum"
      unit        = "Count"

      dimensions = {
        "IDPAggregated" = each.key
      }
    }
  }

  metric_query {
    id = "errors"

    metric {
      metric_name = "IDPError"
      namespace   = var.idp_alarm.namespace
      period      = 60
      stat        = "Sum"
      unit        = "Count"

      dimensions = {
        "IDPAggregated" = each.key
      }
    }
  }
}

resource "aws_cloudwatch_metric_alarm" "client_error_alarm" {
  for_each            = var.client_alarm != null ? { for c in var.client_alarm.clients : c.client_id => c } : {}
  alarm_name          = format("%s_%s_%s_%s", "ClientErrorRateAlarm", var.env_short, each.value.friendly_name, each.key)
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  threshold           = 1
  ok_actions          = [module.update_status_lambda.lambda_function_arn]


  alarm_actions = [
    var.sns_topic_arn,
    module.update_status_lambda.lambda_function_arn
  ]

  metric_query {
    id          = "error_rate"
    expression  = "errors/(successes+errors)*100"
    label       = "Error Rate"
    return_data = "true"
  }

  metric_query {
    id = "successes"

    metric {
      metric_name = "ClientSuccess"
      namespace   = var.client_alarm.namespace
      period      = 60
      stat        = "Sum"
      unit        = "Count"

      dimensions = {
        "ClientAggregated" = each.key
      }
    }
  }

  metric_query {
    id = "errors"

    metric {
      metric_name = "ClientError"
      namespace   = var.client_alarm.namespace
      period      = 60
      stat        = "Sum"
      unit        = "Count"

      dimensions = {
        "ClientAggregated" = each.key
      }
    }
  }
}

/*
resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  count               = var.service_core.autoscaling.enable ? 1 : 0
  alarm_name          = "cpu-high-alarm"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "1"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = "60"
  statistic           = "Average"
  threshold           = 50
  alarm_description   = "This alarm triggers when CPU utilization exceeds 70%."
  dimensions = {
    ClusterName = module.ecs_cluster.cluster_name
    ServiceName = module.ecs_core_service.name
  }

  alarm_actions = [
    module.ecs_core_service.autoscaling_policies["cpu_high"].arn
  ]
}

resource "aws_cloudwatch_metric_alarm" "cpu_low" {
  count               = var.service_core.autoscaling.enable ? 1 : 0
  alarm_name          = "cpu-low-alarm"
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods  = "1"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = "900"
  statistic           = "Average"
  threshold           = 20
  alarm_description   = "This alarm triggers when CPU utilization exceeds 70%."
  dimensions = {
    ClusterName = module.ecs_cluster.cluster_name
    ServiceName = module.ecs_core_service.name
  }

  alarm_actions = [
    module.ecs_core_service.autoscaling_policies["cpu_low"].arn
  ]
}

*/
resource "aws_iam_policy" "deploy_ecs" {
  name        = format("%s-policy", var.service_core.service_name)
  description = "Policy to allow deploy on ECS."

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [

      {
        Sid    = "ECRPublish"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:BatchGetImage",
          "ecr:CompleteLayerUpload",
          "ecr:DescribeImages",
          "ecr:GetAuthorizationToken",
          "ecr:InitiateLayerUpload",
          "ecr:ListImages",
          "ecr:PutImage",
          "ecr:TagResource",
          "ecr:UploadLayerPart",
        ]
        Resource = ["*"]
      },
      {
        Effect = "Allow"
        Action = [
          "ecs:DescribeTaskDefinition",
          "ecs:RegisterTaskDefinition",
          "ecs:DescribeServices",
          "ecs:UpdateService",
        ]
        Resource = "*"
        Sid      = "ECSTaskDefinition"
      },
      {
        Effect = "Allow"
        Action = "iam:PassRole"
        Resource = [
          module.ecs_core_service.tasks_iam_role_arn,
          module.ecs_core_service.task_exec_iam_role_arn,
        ]

        Sid = "PassRole"
      }
    ]
  })
}

resource "aws_iam_policy" "deploy_ecs_internal_idp" {
  count       = var.internal_idp_enabled ? 1 : 0
  name        = format("%s-policy", var.service_internal_idp.service_name)
  description = "Policy to allow deploy internal IDP on ECS."

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [

      {
        Sid    = "ECRPublish"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:BatchGetImage",
          "ecr:CompleteLayerUpload",
          "ecr:DescribeImages",
          "ecr:GetAuthorizationToken",
          "ecr:InitiateLayerUpload",
          "ecr:ListImages",
          "ecr:PutImage",
          "ecr:TagResource",
          "ecr:UploadLayerPart",
        ]
        Resource = ["*"]
      },
      {
        Effect = "Allow"
        Action = [
          "ecs:DescribeTaskDefinition",
          "ecs:RegisterTaskDefinition",
          "ecs:DescribeServices",
          "ecs:UpdateService",
        ]
        Resource = "*"
        Sid      = "ECSTaskDefinition"
      },
      {
        Effect = "Allow"
        Action = "iam:PassRole"
        Resource = [
          module.ecs_internal_idp_service[0].tasks_iam_role_arn,
          module.ecs_internal_idp_service[0].task_exec_iam_role_arn,
        ]

        Sid = "PassRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "deploy_ecs" {
  role       = aws_iam_role.githubecsdeploy.name
  policy_arn = aws_iam_policy.deploy_ecs.arn
}

resource "aws_iam_role_policy_attachment" "deploy_ecs_internal_idp" {
  count      = var.internal_idp_enabled ? 1 : 0
  role       = aws_iam_role.githubecsdeploy_internal_idp[0].name
  policy_arn = aws_iam_policy.deploy_ecs_internal_idp[0].arn
}

## Network load balancer ##

module "elb" {
  source  = "terraform-aws-modules/alb/aws"
  version = "9.8.0"
  name    = var.nlb_name

  load_balancer_type = "network"

  vpc_id                           = var.vpc_id
  subnets                          = var.private_subnets
  enable_cross_zone_load_balancing = "true"

  internal = true

  dns_record_client_routing_policy = "availability_zone_affinity"

  # For example only
  enable_deletion_protection = false

  # Security Group
  enforce_security_group_inbound_rules_on_private_link_traffic = "off"

  # No ingress rules allow only access via private link
  security_group_ingress_rules = {
  }

  security_group_egress_rules = {
    all = {
      ip_protocol = "-1"
      cidr_ipv4   = var.vpc_cidr_block
    }
  }
  listeners = merge(
    { ecs-oneid-core = {
      port     = var.service_core.container.containerPort
      protocol = "TCP"
      forward = {
        target_group_key = "ecs-oneid-core"
      }
    } },
    var.internal_idp_enabled ? {
      ecs-oneid-internal-idp = {
        port     = var.service_internal_idp.container.containerPort
        protocol = "TCP"
        forward = {
          target_group_key = "ecs-oneid-internal-idp"
        }
    } } : {}
  )

  target_groups = merge(
    {
      ecs-oneid-core = {
        name_prefix          = "t1-"
        protocol             = "TCP"
        port                 = var.service_core.container.containerPort
        target_type          = "ip"
        deregistration_delay = 10
        create_attachment    = false
        health_check = {
          enabled             = true
          interval            = 30
          path                = "/q/health/live"
          port                = var.service_core.container.containerPort
          healthy_threshold   = 3
          unhealthy_threshold = 3
          timeout             = 6
        }
      }
    },
    var.internal_idp_enabled ? {
      ecs-oneid-internal-idp = {
        name_prefix          = "t1-"
        protocol             = "TCP"
        port                 = var.service_internal_idp.container.containerPort
        target_type          = "ip"
        deregistration_delay = 10
        create_attachment    = false
        health_check = {
          enabled             = true
          interval            = 30
          path                = "/q/health/live"
          port                = var.service_internal_idp.container.containerPort
          healthy_threshold   = 3
          unhealthy_threshold = 3
          timeout             = 6
        }
      }
    } : {}
  )


  tags = { Name : var.nlb_name }
}

locals {
  service_id = join("/", [
    "service",
    module.ecs_cluster.cluster_name,
    module.ecs_core_service.name
    ]
  )

}

## Iam role to switch region ## 

resource "aws_iam_role" "switch_region_role" {
  count       = var.switch_region_enabled ? 1 : 0
  name        = "${var.role_prefix}-switch-region-role"
  description = "Role to assume to switch region."


  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          "Federated" : "arn:aws:iam::${var.aws_caller_identity}:oidc-provider/token.actions.githubusercontent.com"
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringLike = {
            "token.actions.githubusercontent.com:sub" : "repo:${var.github_repository}:*"
          },
          "ForAllValues:StringEquals" = {
            "token.actions.githubusercontent.com:iss" : "https://token.actions.githubusercontent.com",
            "token.actions.githubusercontent.com:aud" : "sts.amazonaws.com"
          }
        }
      }
    ]
  })
}

resource "aws_iam_policy" "switch_region_policy" {
  count       = var.switch_region_enabled ? 1 : 0
  name        = "${var.role_prefix}-switch-region-policy"
  description = "Policy to switch region"

  policy = jsonencode({

    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "application-autoscaling:RegisterScalableTarget",
          "ecs:UpdateService",
          "ecs:DescribeServices"
        ]
        Resource = [
          "${module.ecs_cluster.cluster_arn}",
          "arn:aws:ecs:${var.aws_region}:${var.aws_caller_identity}:service/${var.ecs_cluster_name}/${var.service_core.service_name}",
          "arn:aws:application-autoscaling:${var.aws_region}:${var.aws_caller_identity}:scalable-target/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "route53:ChangeResourceRecordSets",
          "route53:ListResourceRecordSets"
        ]
        Resource = "arn:aws:route53:::hostedzone/${var.hosted_zone_id}"
      }
    ]
  })

}

resource "aws_iam_role_policy_attachment" "switch_region" {
  count      = var.switch_region_enabled ? 1 : 0
  role       = aws_iam_role.switch_region_role[0].name
  policy_arn = aws_iam_policy.switch_region_policy[0].arn
}

resource "aws_cloudwatch_metric_alarm" "ecs_task_running_core" {
  count               = var.enable_container_insights ? 1 : 0
  alarm_name          = "ecs-task-running-core"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name         = "RunningTaskCount"
  namespace           = "ECS/ContainerInsights"
  period              = 60
  statistic           = "Minimum"
  threshold           = 1
  alarm_description   = "This alarm triggers when ECS Task Running is less than 1."
  dimensions = {
    ClusterName = module.ecs_cluster.cluster_name
    ServiceName = module.ecs_core_service.name
  }

  alarm_actions = [
    var.sns_topic_arn
  ]
}

resource "aws_cloudwatch_metric_alarm" "ecs_task_running_idp" {
  count               = var.enable_container_insights ? 1 : 0
  alarm_name          = "ecs-task-running-idp"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name         = "RunningTaskCount"
  namespace           = "ECS/ContainerInsights"
  period              = 60
  statistic           = "Minimum"
  threshold           = 1
  alarm_description   = "This alarm triggers when ECS Task Running is less than 1."
  dimensions = {
    ClusterName = module.ecs_cluster.cluster_name
    ServiceName = module.ecs_internal_idp_service.name
  }

  alarm_actions = [
    var.sns_topic_arn
  ]
}