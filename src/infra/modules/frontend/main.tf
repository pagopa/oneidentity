## DNS Zones
module "zones" {
  source  = "terraform-aws-modules/route53/aws//modules/zones"
  version = "2.11.0"
  zones   = var.r53_dns_zones
}

module "records" {
  source  = "terraform-aws-modules/route53/aws//modules/records"
  version = "2.11.0"

  zone_name = keys(module.zones.route53_zone_zone_id)[0]

  records = [
    {
      name = ""
      type = "A"
      alias = {
        name                   = module.alb.dns_name
        zone_id                = module.alb.zone_id
        evaluate_target_health = true
      }
    },
  ]

  depends_on = [module.zones]
}

## ACM ##
module "acm" {
  source  = "terraform-aws-modules/acm/aws"
  version = "5.0.0"

  domain_name = keys(var.r53_dns_zones)[0]

  zone_id = module.zones.route53_zone_zone_id[keys(var.r53_dns_zones)[0]]

  validation_method      = "DNS"
  create_route53_records = true

  tags = {
    Name = keys(var.r53_dns_zones)[0]
  }
}


module "alb" {
  source  = "terraform-aws-modules/alb/aws"
  version = "9.7.0"

  name = var.alb_name

  load_balancer_type = "application"

  vpc_id  = var.vpc_id
  subnets = var.public_subnet_ids

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
      cidr_ipv4   = "0.0.0.0/0"
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
      certificate_arn = var.acm_certificate_arn

      forward = {
        target_group_key = "ecs_oneidentity"
      }
    }
  }

  target_groups = {
    ecs_oneidentity = {
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
    Name = var.alb_name
  }
}


## REST API Gateway ##
module "rest_api" {
  source = "../rest-api"

  name = var.rest_api_name

  stage_name = var.rest_api_stage

  endpoint_configuration = {
    #TODO: is this the best endpoint type we need?
    types = ["REGIONAL"]
  }

  body = templatefile("./api/oi.tpl.json",
    {
      server_url    = keys(var.r53_dns_zones)[0]
      uri           = format("http://%s:%s", var.nlb_dns_name, "8080"),
      connection_id = aws_api_gateway_vpc_link.apigw.id
  })


  custom_domain_name        = keys(var.r53_dns_zones)[0]
  create_custom_domain_name = false
  #certificate_arn    = module.acm.acm_certificate_arn
  api_mapping_key = null

}

resource "aws_api_gateway_vpc_link" "apigw" {
  name        = "ApiGwVPCLink"
  description = "VPC link to the private network load balancer."
  target_arns = var.api_gateway_target_arns
}