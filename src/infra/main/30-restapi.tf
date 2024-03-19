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
    servers = [
      {
        url = format("http://%s/v1", var.r53_dns_zone.name),
      }
    ],
    paths = {
      "/" = {
        get = {
          x-amazon-apigateway-integration = {
            type                = "HTTP"
            httpMethod          = "GET"
            uri                 = format("http://%s:%s", module.elb.dns_name, local.container_port),
            connectionType      = "VPC_LINK"
            connectionId        = aws_api_gateway_vpc_link.apigw.id
            requestParameters   = {}
            passthroughBehavior = "WHEN_NO_TEMPLATES",
            responses = {
              200 = {
                statusCode = "200",
                responseParameters = {
                  "method.response.header.Content-Type" = "integration.response.header.Content-Type"
                }

              },
            }
          }
          responses = {
            200 = {
              "$ref" = "#/components/responses/SuccessResponse"
            }
          }
        }
      }
      "/metadata" = {
        get = {
          x-amazon-apigateway-integration = {
            type                = "HTTP"
            httpMethod          = "GET"
            uri                 = format("http://%s:%s/metadata", module.elb.dns_name, local.container_port),
            connectionType      = "VPC_LINK"
            connectionId        = aws_api_gateway_vpc_link.apigw.id
            requestParameters   = {}
            passthroughBehavior = "WHEN_NO_TEMPLATES",
            responses = {
              200 = {
                statusCode = "200",
                responseParameters = {
                  "method.response.header.Content-Type" = "integration.response.header.Content-Type"
                }

              },
            }
          }
          responses = {
            200 = {
              "$ref" = "#/components/responses/SuccessResponse"
            }
          }
        }
      }
      "/spid-login" = {
        get = {
          parameters = [{
            name        = "idp",
            in          = "query",
            description = "Identity Provider URL",
            required    = "true",
            schema = {
              type = "string"
            }
          }]
          x-amazon-apigateway-integration = {
            type           = "HTTP"
            httpMethod     = "GET"
            uri            = format("http://%s:%s/spid-login", module.elb.dns_name, local.container_port),
            connectionType = "VPC_LINK"
            connectionId   = aws_api_gateway_vpc_link.apigw.id
            requestParameters = {
              "integration.request.querystring.idp" : "method.request.querystring.idp"
            }
            passthroughBehavior = "WHEN_NO_TEMPLATES",
            responses = {
              200 = {
                statusCode = "200",
                responseParameters = {
                  "method.response.header.Content-Type" = "integration.response.header.Content-Type"
                }
              },
            }
          }
          responses = {
            200 = {
              "$ref" = "#/components/responses/SuccessResponse"
            }
          }
        }
      }
    }
    components = {
      responses = {
        SuccessResponse = {
          description = "200 response"
          statusCode  = "200"
          headers = {
            Content-Type = {
              schema = {
                type = "string"
              }
            }
          }
          content = {}
        }
      }
    }
  })


  custom_domain_name = var.r53_dns_zone.name
  certificate_arn    = module.acm.acm_certificate_arn

}
