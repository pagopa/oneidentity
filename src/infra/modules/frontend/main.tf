module "records" {
  count   = var.create_dns_record ? 1 : 0
  source  = "terraform-aws-modules/route53/aws//modules/records"
  version = "2.11.0"

  zone_name = var.domain_name

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
    },
    {
      name = "admin"
      type = "A"
      alias = {
        name                   = module.rest_api_admin[0].regional_domain_name
        zone_id                = module.rest_api_admin[0].regional_zone_id
        evaluate_target_health = true
        ttl                    = var.dns_record_ttl
      }
    }
    ]
  )
}

## ACM ##
module "acm" {
  source  = "terraform-aws-modules/acm/aws"
  version = "5.0.0"

  domain_name = var.domain_name

  zone_id = var.r53_dns_zone_id

  validation_method      = "DNS"
  create_route53_records = true

  tags = {
    Name = var.domain_name
  }
}

module "acm_admin" {
  count   = var.aws_region != "eu-south-1" ? 0 : 1
  source  = "terraform-aws-modules/acm/aws"
  version = "5.0.0"

  #domain_name = format("admin.%s", var.domain_admin_name)
  domain_name = var.domain_admin_name != null ? format("admin.%s", var.domain_admin_name) : null

  zone_id = var.r53_dns_zone_id

  validation_method      = "DNS"
  create_route53_records = true

  tags = {
    Name = var.domain_admin_name != null ? format("admin.%s", var.domain_admin_name) : null
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
  name               = "${var.rest_api_name}-s3-proxy-role"
  assume_role_policy = data.aws_iam_policy_document.apigw_assume_role.json
}

data "aws_iam_policy_document" "s3_apigw_proxy" {
  statement {
    effect  = "Allow"
    actions = ["s3:GetObject", "s3:ListBucket"]
    resources = [
      "${var.assets_bucket_arn}/*",
      "${var.assets_bucket_arn}",
    ]
  }
}

resource "aws_iam_policy" "s3_apigw_proxy" {
  name        = "${var.role_prefix}-s3-read-assets"
  description = "Get Object in S3 object."
  policy      = data.aws_iam_policy_document.s3_apigw_proxy.json
}

resource "aws_iam_role_policy_attachment" "s3_apigw_proxy" {
  role       = aws_iam_role.s3_apigw_proxy.name
  policy_arn = aws_iam_policy.s3_apigw_proxy.arn
}

## Role that allows api gateway to invoke lambda functions.
resource "aws_iam_role" "lambda_apigw_proxy" {
  name               = "${var.rest_api_name}-lambda-proxy"
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
  name        = "${var.role_prefix}-apigw-invoke-lambda"
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

  name                 = var.rest_api_name
  stage_name           = var.rest_api_stage
  xray_tracing_enabled = var.xray_tracing_enabled

  endpoint_configuration = {
    #TODO: is this the best endpoint type we need?
    types = ["REGIONAL"]
  }

  body = templatefile(var.openapi_template_file,
    {
      server_url                     = var.domain_name
      uri                            = format("http://%s:%s", var.nlb_dns_name, "8080"),
      connection_id                  = aws_api_gateway_vpc_link.apigw.id
      aws_region                     = var.aws_region
      client_registration_lambda_arn = var.client_registration_lambda_arn
      retrieve_status_lambda_arn     = var.retrieve_status_lambda_arn
      s3_apigateway_proxy_role       = aws_iam_role.s3_apigw_proxy.arn
      lambda_apigateway_proxy_role   = aws_iam_role.lambda_apigw_proxy.arn
      assets_bucket_uri = format("arn:aws:apigateway:%s:s3:path/%s", var.aws_region,
      var.assets_bucket_name)
      authorizer         = var.api_authorizer_name != null ? var.api_authorizer_name : "api_key"
      provider_arn       = var.provider_arn
      cors_allow_origins = var.cors_allow_origins != null ? var.cors_allow_origins : format("https://admin.%s", var.domain_name)
  })


  custom_domain_name        = var.domain_name
  create_custom_domain_name = var.create_custom_domain_name
  certificate_arn           = module.acm.acm_certificate_arn

  plan                      = var.api_gateway_plan
  api_cache_cluster_enabled = var.api_cache_cluster_enabled
  api_cache_cluster_size    = var.api_cache_cluster_size
  method_settings           = var.api_method_settings

  api_authorizer = {
    name          = var.api_authorizer_name == "" ? null : var.api_authorizer_name
    user_pool_arn = var.user_pool_arn == "" ? null : var.user_pool_arn
  }
}

resource "aws_api_gateway_vpc_link" "apigw" {
  name        = "ApiGwVPCLink"
  description = "VPC link to the private network load balancer."
  target_arns = var.api_gateway_target_arns
}

resource "aws_cloudwatch_metric_alarm" "api_alarms" {
  for_each            = var.api_alarms
  alarm_name          = format("%s-%s-%s-%s", module.rest_api.rest_api_name, each.value.resource_name, each.value.metric_name, each.value.threshold)
  comparison_operator = each.value.comparison_operator
  evaluation_periods  = each.value.evaluation_periods
  metric_name         = each.value.metric_name
  namespace           = each.value.namespace
  period              = each.value.period
  statistic           = each.value.statistic
  threshold           = each.value.threshold

  dimensions = {
    ApiName  = module.rest_api.rest_api_name
    Stage    = var.rest_api_stage
    Resource = each.value.resource_name
    Method   = each.value.method
  }

  alarm_actions = [each.value.sns_topic_alarm_arn]
}

## Firewall regional web acl  
resource "aws_wafv2_web_acl" "main" {
  name        = var.web_acl.name
  description = "Api gateway WAF."
  scope       = "REGIONAL"

  visibility_config {
    cloudwatch_metrics_enabled = var.web_acl.cloudwatch_metrics_enabled
    metric_name                = var.web_acl.name
    sampled_requests_enabled   = var.web_acl.sampled_requests_enabled
  }
  default_action {
    allow {}
  }


  dynamic "rule" {
    for_each = { for r in local.web_acl_rules : r.name => r }
    content {
      name     = rule.value.name
      priority = rule.value.priority

      override_action {
        count {}
      }

      statement {
        managed_rule_group_statement {
          name        = rule.value.managed_rule_group_name
          vendor_name = rule.value.vendor_name
        }
      }

      visibility_config {
        cloudwatch_metrics_enabled = var.web_acl.cloudwatch_metrics_enabled
        metric_name                = rule.value.metric_name
        sampled_requests_enabled   = var.web_acl.sampled_requests_enabled
      }
    }
  }

  tags = { Name = var.web_acl.name }
}

resource "aws_wafv2_web_acl_association" "main" {
  resource_arn = "arn:aws:apigateway:${var.aws_region}::/restapis/${module.rest_api.rest_api_id}/stages/${var.rest_api_stage}"
  web_acl_arn  = aws_wafv2_web_acl.main.arn
}

##OpenApi export

data "aws_api_gateway_export" "api_exp" {
  rest_api_id = module.rest_api.rest_api_id
  stage_name  = var.rest_api_stage
  export_type = "oas30"
}

resource "aws_s3_object" "openapi_exp" {
  key              = "static/openapi/oas30.json"
  bucket           = var.assets_bucket_name
  content          = data.aws_api_gateway_export.api_exp.body
  content_encoding = "utf-8"
  content_type     = "application/json"
}

## Alarm

module "webacl_count_alarm" {
  source  = "terraform-aws-modules/cloudwatch/aws//modules/metric-alarms-by-multiple-dimensions"
  version = "5.6.0"

  count = var.web_acl.cloudwatch_metrics_enabled ? 1 : 0

  alarm_name          = "${var.rest_api_name}-"
  alarm_description   = "Alarm when webacl count greater than 10"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 3
  datapoints_to_alarm = 2
  threshold           = 10
  period              = 300
  unit                = "Count"

  namespace   = "AWS/WAFV2"
  metric_name = "CountedRequests"
  statistic   = "Average"

  dimensions = {
    "webacl" = {
      WebACL = aws_wafv2_web_acl.main.name
      Region = var.aws_region
      Rule   = "ALL"
    },
  }

  alarm_actions = [var.web_acl.sns_topic_arn]
}

module "rest_api_admin" {
  source               = "../rest-api"
  count                = var.aws_region != "eu-south-1" ? 0 : 1
  name                 = var.rest_api_admin_name
  stage_name           = var.rest_api_admin_stage
  xray_tracing_enabled = var.xray_tracing_enabled

  body = templatefile(var.openapi_admin_template_file,
    {
      server_url                   = var.domain_name
      aws_region                   = var.aws_region
      client_manager_lambda_arn    = var.client_manager_lambda_arn
      lambda_apigateway_proxy_role = aws_iam_role.lambda_apigw_proxy.arn
      authorizer                   = var.api_authorizer_admin_name != null ? var.api_authorizer_admin_name : "api_key"
      provider_arn                 = var.provider_arn
  })

  custom_domain_name        = format("admin.%s", var.domain_admin_name)
  create_custom_domain_name = var.create_custom_domain_name
  certificate_arn           = module.acm_admin[0].acm_certificate_arn

  plan = var.api_gateway_admin_plan

  api_authorizer = {
    name          = var.api_authorizer_admin_name == "" ? null : var.api_authorizer_admin_name
    user_pool_arn = var.user_pool_arn == "" ? null : var.user_pool_arn
  }

  endpoint_configuration = {
    #TODO: is this the best endpoint type we need?
    types = ["REGIONAL"]
  }

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
