module "ecr" {
  source  = "terraform-aws-modules/ecr/aws"
  version = "1.6.0"

  for_each = {for r in var.ecr_registers : r.name => r}

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
  registry_replication_rules = []
}

## KMS key to sign the Jwt tokens.
module "jwt_sign" {
  source  = "terraform-aws-modules/kms/aws"
  version = "2.2.1"

  description              = "KMS key to sign Jwt tokens"
  key_usage                = "SIGN_VERIFY"
  customer_master_key_spec = "RSA_2048"
  enable_key_rotation = false


  # Aliases
  aliases = ["test-sign-jwt"]
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
        Sid    = "KSMSign"
        Effect = "Allow"
        Action = [
          "kms:Sign",
          "kms:GetPublicKey",
        ]
        Resource = [
          "${module.jwt_sign.aliases.test-sign-jwt.target_key_arn}"
        ]
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
        "Sid" : "SSMGetCertParameters",
        "Effect" : "Allow",
        "Action" : [
          "ssm:GetParameter"
        ],
        "Resource" : [
          "${data.aws_ssm_parameter.certificate.arn}",
          "${data.aws_ssm_parameter.key.arn}"
        ]
      }
    ]
  })

}

data "aws_ssm_parameter" "certificate" {
  name = var.app_cert_name
}

data "aws_ssm_parameter" "key" {
  name = var.app_key_name
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

resource "aws_cloudwatch_log_group" "ecs_core" {
  name = format("/aws/ecs/%s/%s", var.service_core.service_name, var.service_core.container.name)

  retention_in_days = var.service_core.container.logs_retention_days
}

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
          name  = "KMS_KEY_ID"
          value = module.jwt_sign.aliases.test-sign-jwt.target_key_id
        }
      ])

      readonly_root_filesystem = false
    }
  }

  enable_autoscaling       = var.service_core.autoscaling.enable
  autoscaling_min_capacity = var.service_core.autoscaling.min_capacity
  autoscaling_max_capacity = var.service_core.autoscaling.max_capacity
  desired_count            = var.service_core.autoscaling.desired_count

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
      type      = "egress"
      from_port = 0
      to_port   = 0
      protocol  = "-1"
      cidr_blocks = ["0.0.0.0/0"]
    }
  }

}

resource "aws_iam_policy" "deploy_ecs" {
  name = format("%s-policy", var.service_core.service_name)
  description = "Policy to allow deploy on ECS."

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [

      {
        Sid    = "ECRPublish"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
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

resource "aws_iam_role_policy_attachment" "deploy_ecs" {
  role       = aws_iam_role.githubecsdeploy.name
  policy_arn = aws_iam_policy.deploy_ecs.arn
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

  listeners = {

    ecs-oneid-core = {
      port     = var.service_core.container.containerPort
      protocol = "TCP"
      forward = {
        target_group_key = "ecs-oneid-core"
      }
    }

  }

  target_groups = {

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
        path                = "/ping"
        port                = var.service_core.container.containerPort
        healthy_threshold   = 3
        unhealthy_threshold = 3
        timeout             = 6
      }
    }
  }


  tags = { Name : var.nlb_name }
}

resource "aws_cloudwatch_metric_alarm" "ecs_alarms" {
  for_each = var.ecs_alarms
  alarm_name = format("%s-%s-High-%s", module.ecs_core_service.id, each.value.metric_name,
    module.ecs_core_service.autoscaling_policies.cpu.target_tracking_scaling_policy_configuration[0].target_value)
  comparison_operator = each.value.comparison_operator
  evaluation_periods  = each.value.evaluation_periods
  metric_name         = each.value.metric_name
  namespace           = each.value.namespace
  period              = each.value.period
  statistic           = each.value.statistic
  threshold           = module.ecs_core_service.autoscaling_policies.cpu.target_tracking_scaling_policy_configuration[0].target_value


  dimensions = {
    ClusterName = module.ecs_cluster.cluster_name
    ServiceName = module.ecs_core_service.name
  }

  alarm_actions = [each.value.sns_topic_alarm_arn]
}
