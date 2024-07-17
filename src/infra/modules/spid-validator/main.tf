module "ecr" {
  source                            = "terraform-aws-modules/ecr/aws"
  version                           = "1.6.0"
  repository_name                   = var.ecr_repository_name
  repository_read_write_access_arns = []
  repository_image_tag_mutability   = var.repository_image_tag_mutability

  repository_lifecycle_policy = jsonencode({
    rules = [
      {
        rulePriority = 1,
        description  = "Keep last 3 images",
        selection = {
          tagStatus   = "untagged",
          countType   = "imageCountMoreThan",
          countNumber = 3
        },
        action = {
          type = "expire"
        }
      }
    ]
  })
}

resource "aws_cloudwatch_log_group" "ecs_spid_validator" {
  name              = format("/aws/ecs/%s/%s", var.spid_validator.service_name, var.spid_validator.container.name)
  retention_in_days = var.spid_validator.container.logs_retention_days
}

module "ecs_spid_validator" {
  source  = "terraform-aws-modules/ecs/aws//modules/service"
  version = "5.9.1"

  name = var.spid_validator.service_name

  cluster_arn = var.spid_validator.cluster_arn

  cpu                    = var.spid_validator.cpu
  memory                 = var.spid_validator.memory
  enable_execute_command = false

  /*
  tasks_iam_role_policies = {
    ecs_core_task = aws_iam_policy.ecs_core_task.arn
  }
  */

  container_definitions = {
    "${var.spid_validator.container.name}" = {
      cpu    = var.spid_validator.container.cpu
      memory = var.spid_validator.container.memory

      essential = true
      image     = "${module.ecr.repository_url}:${var.spid_validator.container.image_version}",

      port_mappings = [
        {
          name          = var.spid_validator.container.name
          containerPort = 8080
          hostPort      = 8080
          protocol      = "tcp"
        }
      ]

      log_configuration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.ecs_spid_validator.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
          mode                  = "non-blocking"
        }
      }

      readonly_root_filesystem = false
    }
  }

  subnet_ids       = var.private_subnets_ids
  assign_public_ip = false

  load_balancer = {
    service = {
      target_group_arn = module.alb.target_groups["spid_validator"].arn
      container_name   = var.spid_validator.container.name
      container_port   = 8080
    }
  }

  security_group_rules = {
    alb_ingress_8080 = {
      type        = "ingress"
      from_port   = 8080
      to_port     = 8080
      protocol    = "tcp"
      description = "Service port"

      source_security_group_id = module.alb.security_group_id
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

## ALB spid validator ##

locals {
  domain_name = format("validator.%s", var.zone_name)
}

module "acm_validator" {
  source  = "terraform-aws-modules/acm/aws"
  version = "5.0.0"

  domain_name = local.domain_name

  zone_id = var.zone_id

  validation_method      = "DNS"
  create_route53_records = true

  tags = {
    Name = local.domain_name
  }
}

module "alb" {
  source  = "terraform-aws-modules/alb/aws"
  version = "9.8.0"

  name = var.alb_spid_validator_name

  load_balancer_type = "application"

  vpc_id  = var.vpc_id
  subnets = var.public_subnet_ids

  # Security Group
  security_group_ingress_rules = {
    all_https = {
      from_port   = 443
      to_port     = 443
      ip_protocol = "tcp"
      cidr_ipv4   = "0.0.0.0/0"
    }
    all_http = {
      from_port   = 80
      to_port     = 80
      ip_protocol = "tcp"
      cidr_ipv4   = "0.0.0.0/0"
    }
  }
  security_group_egress_rules = {
    all = {
      ip_protocol = "-1"
      cidr_ipv4   = var.vpc_cidr_block
    }
  }

  listeners = {
    ex_http = {
      port     = 80
      protocol = "HTTP"

      redirect = {
        port        = "443"
        protocol    = "HTTPS"
        status_code = "HTTP_301"
      }
    }

    ex_https = {
      port            = 443
      protocol        = "HTTPS"
      ssl_policy      = "ELBSecurityPolicy-TLS13-1-2-Res-2021-06"
      certificate_arn = module.acm_validator.acm_certificate_arn

      forward = {
        target_group_key = "spid_validator"
      }

    }
  }

  target_groups = {
    spid_validator = {
      backend_protocol                  = "HTTP"
      backend_port                      = 8080
      target_type                       = "ip"
      deregistration_delay              = 5
      load_balancing_cross_zone_enabled = true

      health_check = {
        enabled             = true
        healthy_threshold   = 5
        interval            = 30
        matcher             = "200"
        path                = "/"
        port                = "traffic-port"
        protocol            = "HTTP"
        timeout             = 20
        unhealthy_threshold = 2
      }

      create_attachment = false
    }
  }
}

module "record" {
  source  = "terraform-aws-modules/route53/aws//modules/records"
  version = "~> 3.0"

  zone_name = var.zone_name

  records = [
    {
      name = "validator"
      type = "A"
      alias = {
        name                   = module.alb.dns_name
        zone_id                = module.alb.zone_id
        evaluate_target_health = true
      }
    }
  ]

}