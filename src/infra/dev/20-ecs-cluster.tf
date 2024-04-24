module "alb" {
  source  = "terraform-aws-modules/alb/aws"
  version = "9.7.0"

  name = local.alb_name

  load_balancer_type = "application"

  vpc_id  = module.network.vpc_id
  subnets = module.network.public_subnet_ids

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
      cidr_ipv4   = module.network.vpc_cidr_block
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
      certificate_arn = module.network.acm_certificate_arn

      forward = {
        target_group_key = "ecs_oneidentity"
      }
    }
  }

  target_groups = {
    ecs_oneidentity = {
      backend_protocol                  = "HTTP"
      backend_port                      = local.container_poc1_port
      target_type                       = "ip"
      deregistration_delay              = 5
      load_balancing_cross_zone_enabled = true

      health_check = {
        enabled             = true
        healthy_threshold   = 5
        interval            = 30
        matcher             = "200"
        path                = "/ping"
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

/*
module "ecs_service_poc2" {
  source  = "terraform-aws-modules/ecs/aws//modules/service"
  version = "5.9.1"

  name = "oneidentity_poc2"

  cluster_arn = module.ecs.cluster_arn

  cpu    = 1024
  memory = 2048

  enable_execute_command = true

  container_definitions = {
    "${local.container_name}" = {
      cpu    = 1024
      memory = 2048

      essential = true
      image     = "${module.ecr.repository_url}:${var.poc2_image_version}",

    
      environment = [
        {
          name  = "BASE_PATH"
          value = "/"
        }
      ]
      

      port_mappings = [
        {
          name          = "oneidentity_poc2"
          containerPort = local.container_poc2_port
          hostPort      = local.container_poc2_port
          protocol      = "tcp"
        }
      ]
    }
  }

  subnet_ids       = module.network.private_subnet_ids
  assign_public_ip = false

  load_balancer = {
    service = {
      #target_group_arn = module.alb.target_groups["ecs_oneidentity"].arn
      target_group_arn = module.elb.target_groups["ecs-two"].arn
      container_name   = local.container_name
      container_port   = local.container_poc2_port
    }
  }

  security_group_rules = {
    alb_ingress_3000 = {
      type                     = "ingress"
      from_port                = local.container_poc1_port
      to_port                  = local.container_poc2_port
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

  depends_on = [
    module.elb,
  ]

}

*/

/*
resource "aws_security_group_rule" "allow_all_https_poc2" {
  type        = "egress"
  from_port   = 443
  to_port     = 443
  protocol    = "tcp"
  description = "Allow outbound https traffic."
  # TODO it might be to0 open.
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = module.ecs_service_poc2.security_group_id
}
*/