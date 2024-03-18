resource "aws_api_gateway_vpc_link" "apigw" {
  name        = format("%s-vpc-link", local.project)
  description = "VPC link to the private network load balancer."
  target_arns = [module.elb.arn]
}



module "poc_v1" {
  source = "../modules/rest-api"

  name = "pocv1"

  stage_name = "v1"

  endpoint_configuration = {
    types = ["REGIONAL"]
  }


  body = jsonencode({
    openapi = "3.0.1"
    info = {
      title   = "PocV1"
      version = "1.0"
    }
    paths = {
      "/" = {
        get = {
          x-amazon-apigateway-integration = {
            type              = "HTTP"
            httpMethod        = "GET"
            uri               = format("http://%s:%s", module.elb.dns_name, local.container_port),
            connectionType    = "VPC_LINK"
            connectionId      = aws_api_gateway_vpc_link.apigw.id
            requestParameters = {}
          }
        }
      }
    }
  })
}