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
        ttl                    = var.dns_record_ttl
      }
    }]
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


data "aws_iam_policy_document" "apigw_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["apigateway.amazonaws.com"]
    }
  }
}

resource "random_id" "suffix" {
  byte_length = 8
}

## Role that allow api gateway to read object in S3

resource "aws_iam_role" "s3_apigw_proxy" {
  name               = "S3ApiGatewayProxy-${random_id.suffix.hex}"
  assume_role_policy = data.aws_iam_policy_document.apigw_assume_role.json
}

data "aws_iam_policy_document" "s3_apigw_proxy" {
  statement {
    effect    = "Allow"
    actions   = ["s3:GetObject"]
    resources = ["${var.assets_bucket_arn}/*"]
  }
}

resource "aws_iam_policy" "s3_apigw_proxy" {
  name        = "S3AssetsGetObject"
  description = "Get Object in S3 object."
  policy      = data.aws_iam_policy_document.s3_apigw_proxy.json
}

resource "aws_iam_role_policy_attachment" "s3_apigw_proxy" {
  role       = aws_iam_role.s3_apigw_proxy.name
  policy_arn = aws_iam_policy.s3_apigw_proxy.arn
}

## Role that allows api gateway to invoke lambda functions.
resource "aws_iam_role" "lambda_apigw_proxy" {
  name               = "LambdaApiGatewayProxy-${random_id.suffix.hex}"
  assume_role_policy = data.aws_iam_policy_document.apigw_assume_role.json
}

data "aws_iam_policy_document" "lambda_apigw_proxy" {
  statement {
    effect    = "Allow"
    actions   = ["lambda:InvokeFunction"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "lambda_apigw_proxy" {
  name        = "LambdaInvoke"
  description = "Lambda invoke policy"
  policy      = data.aws_iam_policy_document.lambda_apigw_proxy.json
}

resource "aws_iam_role_policy_attachment" "lambda_apigw_proxy" {
  role       = aws_iam_role.lambda_apigw_proxy.name
  policy_arn = aws_iam_policy.lambda_apigw_proxy.arn
}

## REST API Gateway ##
module "rest_api" {
  source = "../rest-api"

  name = var.rest_api_name

  stage_name           = var.rest_api_stage
  xray_tracing_enabled = var.xray_tracing_enabled

  endpoint_configuration = {
    #TODO: is this the best endpoint type we need?
    types = ["REGIONAL"]
  }

  body = templatefile("../api/oi.tpl.json",
    {
      server_url                     = keys(var.r53_dns_zones)[0]
      uri                            = format("http://%s:%s", var.nlb_dns_name, "8080"),
      connection_id                  = aws_api_gateway_vpc_link.apigw.id
      aws_region                     = var.aws_region
      metadata_lambda_arn            = var.metadata_lamba_arn
      client_registration_lambda_arn = var.client_registration_lambda_arn
      s3_apigateway_proxy_role       = aws_iam_role.s3_apigw_proxy.arn
      lambda_apigateway_proxy_role   = aws_iam_role.lambda_apigw_proxy.arn
      assets_bucket_uri = format("arn:aws:apigateway:%s:s3:path/%s", var.aws_region,
      var.assets_bucket_name)
  })


  custom_domain_name        = keys(var.r53_dns_zones)[0]
  create_custom_domain_name = true
  certificate_arn           = module.acm.acm_certificate_arn

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

/*
## REST API Gateway ##
module "static_content" {
  source = "../rest-api"

  name = var.rest_api_name

  stage_name           = "v1"
  xray_tracing_enabled = false

  endpoint_configuration = {
    #TODO: is this the best endpoint type we need?
    types = ["REGIONAL"]
  }

  body = templatefile("../api/static-content-oi-tpl.json",
    {
      #bucket_name = "todo"
  })


  custom_domain_name        = keys(var.r53_dns_zones)[0]
  create_custom_domain_name = false
  #certificate_arn           = module.acm.acm_certificate_arn

  plan                      = {
    name = "static"
    api_key_name = null
    throttle_rate_limit = 100
    throttle_burst_limit = 200
  }
  api_cache_cluster_enabled = true
  api_cache_cluster_size    = var.api_cache_cluster_size
  method_settings           = []

}

*/
