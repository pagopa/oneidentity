resource "aws_api_gateway_rest_api" "main" {
  name = var.name
  body = var.body

  // set to merge when the vpc endpoint is private.
  // put_rest_api_mode = "merge"

  endpoint_configuration {
    types            = var.endpoint_configuration.types
    vpc_endpoint_ids = lookup(var.endpoint_configuration, "vpc_endpoint_ids", [])
  }

  disable_execute_api_endpoint = var.create_custom_domain_name ? true : false
  tags = {
    Name = var.name
  }

}

resource "aws_api_gateway_deployment" "main" {
  rest_api_id = aws_api_gateway_rest_api.main.id

  triggers = {
    redeployment = sha1(jsonencode(aws_api_gateway_rest_api.main.body))
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "main" {
  deployment_id = aws_api_gateway_deployment.main.id
  rest_api_id   = aws_api_gateway_rest_api.main.id
  stage_name    = var.stage_name

  cache_cluster_enabled = var.api_cache_cluster_enabled
  cache_cluster_size    = var.api_cache_cluster_size

  xray_tracing_enabled = var.xray_tracing_enabled

  tags = {
    Name = format("%s-%s", var.name, var.stage_name)
  }
}

resource "aws_api_gateway_usage_plan" "main" {
  name        = var.plan.name
  description = "Usage plan for ${var.name} "

  api_stages {
    api_id = aws_api_gateway_rest_api.main.id
    stage  = aws_api_gateway_stage.main.stage_name
  }

  throttle_settings {
    burst_limit = var.plan.throttle_burst_limit
    rate_limit  = var.plan.throttle_rate_limit
  }
}

resource "aws_api_gateway_api_key" "main" {
  count = var.plan.api_key_name != null ? 1 : 0
  name  = var.plan.api_key_name
  tags = {
    Name = var.plan.api_key_name
  }
}

resource "aws_api_gateway_usage_plan_key" "main" {
  count         = var.plan.api_key_name != null ? 1 : 0
  key_id        = aws_api_gateway_api_key.main[0].id
  key_type      = "API_KEY"
  usage_plan_id = aws_api_gateway_usage_plan.main.id
}



## API Gateway cloud watch logs
resource "aws_iam_role" "apigw" {
  name = format("%sRole", var.name)

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AssumeRole",
      "Effect": "Allow",
      "Principal": {
        "Service": "apigateway.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy" "cloudwatch" {
  name = "default"
  role = aws_iam_role.apigw.id

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:DescribeLogGroups",
                "logs:DescribeLogStreams",
                "logs:PutLogEvents",
                "logs:GetLogEvents",
                "logs:FilterLogEvents"
            ],
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_api_gateway_account" "main" {
  cloudwatch_role_arn = aws_iam_role.apigw.arn
}

resource "aws_cloudwatch_log_group" "main" {
  name              = "API-Gateway-Execution-Logs_${aws_api_gateway_rest_api.main.id}/${var.stage_name}"
  retention_in_days = 7

}

resource "aws_api_gateway_method_settings" "main" {
  for_each = { for i in var.method_settings : i.method_path => i }

  rest_api_id = aws_api_gateway_rest_api.main.id
  stage_name  = aws_api_gateway_stage.main.stage_name

  method_path = each.value.method_path

  settings {
    metrics_enabled                         = each.value.metrics_enabled
    logging_level                           = each.value.logging_level
    data_trace_enabled                      = each.value.data_trace_enabled
    throttling_rate_limit                   = each.value.throttling_rate_limit
    throttling_burst_limit                  = each.value.throttling_burst_limit
    caching_enabled                         = each.value.caching_enabled
    cache_ttl_in_seconds                    = each.value.cache_ttl_in_seconds
    cache_data_encrypted                    = each.value.cache_data_encrypted
    require_authorization_for_cache_control = each.value.require_authorization_for_cache_control
  }
}

resource "aws_api_gateway_domain_name" "main" {
  count                    = var.create_custom_domain_name ? 1 : 0
  domain_name              = var.custom_domain_name
  regional_certificate_arn = var.certificate_arn

  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_apigatewayv2_api_mapping" "main" {
  count       = var.custom_domain_name != null ? 1 : 0
  api_id      = aws_api_gateway_rest_api.main.id
  stage       = var.stage_name
  domain_name = var.custom_domain_name
}

resource "aws_api_gateway_authorizer" "main" {
  count         = var.api_authorizer.name != "" ? 1 : 0
  name          = var.api_authorizer.name
  rest_api_id   = aws_api_gateway_rest_api.main.id
  type          = "COGNITO_USER_POOLS"
  provider_arns = [var.api_authorizer.user_pool_arn]
}