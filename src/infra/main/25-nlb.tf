module "elb" {
  source  = "terraform-aws-modules/alb/aws"
  version = "9.8.0"
  name    = format("%s-nlb", local.project)

  load_balancer_type = "network"

  vpc_id                           = module.vpc.vpc_id
  subnets                          = module.vpc.private_subnets
  enable_cross_zone_load_balancing = "true"

  internal = true

  dns_record_client_routing_policy = "availability_zone_affinity"

  # For example only
  enable_deletion_protection = false

  # Security Group
  enforce_security_group_inbound_rules_on_private_link_traffic = "off"

  security_group_ingress_rules = {
    all_tcp = {
      from_port   = local.container_poc1_port
      to_port     = local.container_poc2_port
      ip_protocol = "tcp"
      description = "TCP traffic"
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
    ecs-one = {
      port     = 8000
      protocol = "TCP"
      forward = {
        target_group_key = "ecs-one"
      }
    }
    ecs-two = {
      port     = 8080
      protocol = "TCP"
      forward = {
        target_group_key = "ecs-two"
      }
    }
  }

  target_groups = {
    ecs-one = {
      name_prefix          = "t1-"
      protocol             = "TCP"
      port                 = 8000
      target_type          = "ip"
      deregistration_delay = 10
      create_attachment    = false
      health_check = {
        enabled             = true
        interval            = 30
        path                = "/"
        port                = 8000
        healthy_threshold   = 3
        unhealthy_threshold = 3
        timeout             = 6
      }
    }

    ecs-two = {
      name_prefix          = "t2-"
      protocol             = "TCP"
      port                 = 8080
      target_type          = "ip"
      deregistration_delay = 10
      create_attachment    = false
      health_check = {
        enabled             = true
        interval            = 30
        path                = "/"
        port                = 8080
        healthy_threshold   = 3
        unhealthy_threshold = 3
        timeout             = 6
      }
    }
  }


  tags = { Name : format("%s-nlb", local.project) }
}