resource "aws_api_gateway_rest_api" "main" {
  name = var.name
  body = var.body

  put_rest_api_mode = "merge"

  endpoint_configuration {
    types            = var.endpoint_configuration.types
    vpc_endpoint_ids = var.endpoint_configuration.vpc_endpoint_ids
  }

  tags = {
    Name = var.name
  }

}

resource "aws_api_gateway_deployment" "main" {
  rest_api_id = aws_api_gateway_rest_api.main.id

  triggers = {
    redeployment = sha1(jsonencode(aws_api_gateway_rest_api.example.body))
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "main" {
  deployment_id = aws_api_gateway_deployment.main.id
  rest_api_id   = aws_api_gateway_rest_api.main.id
  stage_name    = var.stage_name

  tags = {
    Name = fomrat("%s-%s", var.name, var.stage_name)
  }
}