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
        name                   = module.rest_api.regional_domain_name
        zone_id                = module.rest_api.regional_zone_id
        evaluate_target_health = true
      }
    },
    {
      name = "validator"
      type = "A"
      alias = {
        name                   = module.alb_spid_validator[0].dns_name
        zone_id                = module.alb_spid_validator[0].zone_id
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
      server_url          = keys(var.r53_dns_zones)[0]
      uri                 = format("http://%s:%s", var.nlb_dns_name, "8080"),
      connection_id       = aws_api_gateway_vpc_link.apigw.id
      aws_region          = var.aws_region
      metadata_lambda_arn = var.metadata_lamba_arn
  })


  custom_domain_name        = keys(var.r53_dns_zones)[0]
  create_custom_domain_name = true
  certificate_arn           = module.acm.acm_certificate_arn
  api_mapping_key           = null

  plan                      = var.api_gateway_plan
  api_cache_cluster_enabled = var.api_cache_cluster_enabled
  api_cache_cluster_size    = var.api_cache_cluster_size
  method_settings           = var.api_method_settings

}

resource "aws_api_gateway_vpc_link" "apigw" {
  name        = "ApiGwVPCLink"
  description = "VPC link to the private network load balancer."
  target_arns = var.api_gateway_target_arns
}

resource "aws_lambda_permission" "allow_api_gw_invoke_metadata" {
  statement_id  = "allowInvokeLambdaMetadata"
  action        = "lambda:InvokeFunction"
  function_name = var.metadata_lamba_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${module.rest_api.rest_api_execution_arn}/*/GET/saml/*/metadata"
}


## ALB spid validator

module "acm_validator" {
  source  = "terraform-aws-modules/acm/aws"
  version = "5.0.0"

  domain_name = format("validator.%s", keys(var.r53_dns_zones)[0])

  zone_id = module.zones.route53_zone_zone_id[keys(var.r53_dns_zones)[0]]

  validation_method      = "DNS"
  create_route53_records = true

  tags = {
    Name = keys(var.r53_dns_zones)[0]
  }
}

module "alb_spid_validator" {
  count = var.create_alb_spid_validator ? 1 : 0
  source  = "terraform-aws-modules/alb/aws"
  version = "9.8.0"

  name = var.alb_name
  
  load_balancer_type = "application"

  vpc_id  = var.vpc_id
  subnets = var.public_subnet_ids

  # Security Group
  security_group_ingress_rules = {
    all_http = {
      from_port   = 443
      to_port     = 443
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
      port = 443
      protocol = "HTTPS"
      ssl_policy                  = "ELBSecurityPolicy-TLS13-1-2-Res-2021-06"
      certificate_arn             = module.acm_validator.acm_certificate_arn

      forward = {
        target_group_key = "spid_validator"
      }

    }
  }

  target_groups = {
    spid_validator = {
      backend_protocol                  = "HTTP"
      backend_port                      = 8443
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
        protocol            = "HTTPS"
        timeout             = 20
        unhealthy_threshold = 2
      }

      create_attachment = false
    }
  }
}
