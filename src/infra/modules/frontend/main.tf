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

  records = concat([
    {
      name = ""
      type = "A"
      alias = {
        name                   = module.rest_api.regional_domain_name
        zone_id                = module.rest_api.regional_zone_id
        evaluate_target_health = true
      }
    }],
    var.create_alb_spid_validator == true ?
    [{
      name = "validator"
      type = "A"
      alias = {
        name                   = module.alb_spid_validator[0].dns_name
        zone_id                = module.alb_spid_validator[0].zone_id
        evaluate_target_health = true
      }
    }] : []
  )

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
