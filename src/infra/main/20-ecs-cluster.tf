locals {
  container_port = 8000
  container_name = "oneidentity"
  alb_name       = format("%s-alb", local.project)
}


module "ecr" {
  source  = "terraform-aws-modules/ecr/aws"
  version = "1.6.0"

  repository_name = format("%s-ecr", local.project)

  repository_lifecycle_policy = jsonencode({
    rules = [
      {
        rulePriority = 1,
        description  = "Keep last ${var.ecr_keep_images} images",
        selection = {
          "tagStatus" : "untagged",
          "countType" : "imageCountMoreThan",
          "countNumber" : var.ecr_keep_images
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


module "ecs" {

  source  = "terraform-aws-modules/ecs/aws"
  version = "5.9.1"

  cluster_name = format("%s-ecs", local.project)

  cluster_settings = [{
    name  = "containerInsights"
    value = var.ecs_enable_container_insights ? "enabled" : "disabled"
    }
  ]

  # Capacity provider
  fargate_capacity_providers = {
    FARGATE = {
      default_capacity_provider_strategy = {
        weight = 50
        base   = 20
      }
    }
  }
}


module "ecs_service" {
  source  = "terraform-aws-modules/ecs/aws//modules/service"
  version = "5.9.1"

  name = "oneidentity"

  cluster_arn = module.ecs.cluster_arn

  cpu    = 512
  memory = 1024

  enable_execute_command = true

  container_definitions = {
    "${local.container_name}" = {
      cpu    = 512
      memory = 1024

      essential = true
      image     = "${module.ecr.repository_url}:2.0",

      port_mappings = [
        {
          name          = "oneidentity"
          containerPort = local.container_port
          hostPort      = local.container_port
          protocol      = "tcp"
        }
      ]
    }
  }

  subnet_ids       = module.vpc.private_subnets
  assign_public_ip = false

  load_balancer = {
    service = {
      #target_group_arn = module.alb.target_groups["ecs_oneidentity"].arn
      target_group_arn = module.elb.target_groups["ecs-one"].arn
      container_name   = local.container_name
      container_port   = local.container_port
    }
  }

  security_group_rules = {
    alb_ingress_3000 = {
      type                     = "ingress"
      from_port                = local.container_port
      to_port                  = local.container_port
      protocol                 = "tcp"
      description              = "Service port"
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


resource "aws_security_group_rule" "allow_all_https" {
  type        = "egress"
  from_port   = 443
  to_port     = 443
  protocol    = "tcp"
  description = "Allow outbound https traffic."
  # TODO it might be to open.
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = module.ecs_service.security_group_id
}


## ALB Certificate ##
module "acm" {
  source  = "terraform-aws-modules/acm/aws"
  version = "5.0.0"

  domain_name = var.r53_dns_zone.name

  validation_method      = "DNS"
  create_route53_records = false

  tags = {
    Name = format("%s-acm", local.project)
  }
}

module "alb" {
  source  = "terraform-aws-modules/alb/aws"
  version = "9.7.0"

  name = local.alb_name

  load_balancer_type = "application"

  vpc_id  = module.vpc.vpc_id
  subnets = module.vpc.public_subnets

  # For example only
  enable_deletion_protection = false

  # Security Group
  security_group_ingress_rules = {
    all_http = {
      from_port   = 80
      to_port     = 80
      ip_protocol = "tcp"
      cidr_ipv4   = "0.0.0.0/0"
    }
    all_https = {
      from_port   = 443
      to_port     = 443
      ip_protocol = "tcp"
      cidr_ipv4   = "0.0.0.0/0"
    }
  }
  security_group_egress_rules = {
    all = {
      ip_protocol = "-1"
      cidr_ipv4   = module.vpc.vpc_cidr_block
    }
  }

  listeners = {
    ex-http-https-redirect = {
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
      certificate_arn = module.acm.acm_certificate_arn

      forward = {
        target_group_key = "ecs_oneidentity"
      }
    }
  }

  target_groups = {
    ecs_oneidentity = {
      backend_protocol                  = "HTTP"
      backend_port                      = local.container_port
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
        timeout             = 5
        unhealthy_threshold = 2
      }

      # There's nothing to attach here in this definition. Instead,
      # ECS will attach the IPs of the tasks to this target group
      create_attachment = false
    }
  }

  tags = {
    Name = format("%s-alb", local.project)
  }
}