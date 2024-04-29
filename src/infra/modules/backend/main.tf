module "ecr" {
  source  = "terraform-aws-modules/ecr/aws"
  version = "1.6.0"

  for_each = { for r in var.ecr_registers : r.name => r }

  repository_name = each.key

  repository_read_write_access_arns = []

  repository_lifecycle_policy = jsonencode({
    rules = [
      {
        rulePriority = 1,
        description  = "Keep last ${each.value.number_of_images_to_keep} images",
        selection = {
          "tagStatus" : "untagged",
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

resource "aws_iam_policy" "ecs_idp_task" {
  name = format("%s-task-policy", var.service_idp.service_name)
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "DynamoDBRW"
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
        ]
        Resource = [
          "${var.table_saml_responces_arn}"
        ]
      }
    ]
  })

}

module "ecs" {

  source  = "terraform-aws-modules/ecs/aws"
  version = "5.9.1"

  cluster_name = var.ecs_cluster_name

  cluster_settings = [{
    name  = "containerInsights"
    value = var.enable_container_insights ? "enabled" : "disabled"
    }
  ]

  # Capacity provider
  fargate_capacity_providers = var.fargate_capacity_providers
}


module "ecs_idp_service" {
  source  = "terraform-aws-modules/ecs/aws//modules/service"
  version = "5.9.1"

  name = var.service_idp.service_name

  cluster_arn = module.ecs.cluster_arn

  cpu    = var.service_idp.cpu
  memory = var.service_idp.memory

  enable_execute_command = var.service_idp.enable_execute_command

  task_exec_iam_role_policies = {
    ecs_idp_task = aws_iam_policy.ecs_idp_task.arn
  }
  container_definitions = {
    "${var.service_idp.container.name}" = {
      cpu    = var.service_idp.container.cpu
      memory = var.service_idp.memory

      essential = true
      image     = "${module.ecr[var.service_idp.container.image_name].repository_url}:${var.service_idp.container.image_version}",

      port_mappings = [
        {
          name          = var.service_idp.container.name
          containerPort = var.service_idp.container.containerPort
          hostPort      = var.service_idp.container.hostPort
          protocol      = "tcp"
        }
      ]
    }
  }

  enable_autoscaling       = var.service_idp.autoscaling.enable
  autoscaling_min_capacity = var.service_idp.autoscaling.min_capacity
  autoscaling_max_capacity = var.service_idp.autoscaling.max_capacity

  subnet_ids       = var.service_idp.subnet_ids
  assign_public_ip = false

  load_balancer = {
    service = {
      target_group_arn = var.service_idp.load_balancer.target_group_arn
      container_name   = var.service_idp.container.name
      container_port   = var.service_idp.container.containerPort
    }
  }

  security_group_rules = {
    alb_ingress_3000 = {
      type                     = "ingress"
      from_port                = var.service_idp.container.containerPort
      to_port                  = var.service_idp.container.containerPort
      protocol                 = "tcp"
      description              = "Service port"
      source_security_group_id = var.service_idp.load_balancer.security_group_id
      #source_security_group_id = module.elb.security_group_id
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


## Deploy with github action
resource "aws_iam_role" "githubecsdeploy" {
  name        = format("%s-deploy", var.service_idp.service_name)
  description = "Role to assume to deploy ECS tasks"


  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Federated = "arn:aws:iam::${var.account_id}:oidc-provider/token.actions.githubusercontent.com"
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringLike = {
            "token.actions.githubusercontent.com:sub" : [
              "repo:${var.github_repository}:*"
            ]
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

/*
data "aws_iam_policy" "ec2_ecr_full_access" {
  name = "AmazonEC2ContainerRegistryFullAccess"
}

resource "aws_iam_role_policy_attachment" "deploy_ec2_ecr_full_access" {
  role       = aws_iam_role.githubecsdeploy.name
  policy_arn = data.aws_iam_policy.ec2_ecr_full_access.arn
}
*/

resource "aws_iam_policy" "deploy_ecs" {
  name        = format("%s-policy", var.service_idp.service_name)
  description = "Policy to allow deploy on ECS."

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      
      {
        Sid = "ECRPublish"
        Effect = "Allow"
        Action = [
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
        Effect   = "Allow"
        Action   = "iam:PassRole"
        Resource = module.ecs_idp_service.task_exec_iam_role_arn
        Sid      = "PassRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "deploy_ecs" {
  role       = aws_iam_role.githubecsdeploy.name
  policy_arn = aws_iam_policy.deploy_ecs.arn
}