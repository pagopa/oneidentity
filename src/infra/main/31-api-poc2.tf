locals {
  stage_name_poc2 = "v2"
}


module "poc_v2" {
  source = "../modules/rest-api"

  name = "pocv2"

  stage_name = local.stage_name_poc2

  endpoint_configuration = {
    types = ["REGIONAL"]
  }

  body = jsonencode({
    openapi = "3.0.1"
    info = {
      title   = "PocV2"
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
            uri                 = format("http://%s:%s", module.elb.dns_name, local.container_poc2_port),
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
            uri                 = format("http://%s:%s/metadata", module.elb.dns_name, local.container_poc2_port)
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
            uri            = format("http://%s:%s/spid-login", module.elb.dns_name, local.container_poc2_port),
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
      spid-sso = {
        post = {
          x-amazon-apigateway-integration = {
            type                = "HTTP"
            httpMethod          = "POST"
            uri                 = format("http://%s:%s/spid-sso", module.elb.dns_name, local.container_poc2_port),
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
              }
              400 = {
                statusCode = "400",
                responseParameters = {
                  "method.response.header.Content-Type" = "integration.response.header.Content-Type"
                }
              }
            }
          }
          responses = {
            200 = {
              "$ref" = "#/components/responses/SuccessResponse"
            }
            400 = {
              "$ref" = "#/components/responses/BadRequest"
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
        BadRequest = {
          description = "400 bad request"
          statusCode  = "400"
          headers = {
            Content-Type = {
              schema = {
                type = "string"
              }
            }
          }
        }
      }
    }
  })


  #custom_domain_name = var.r53_dns_zone.name
  #certificate_arn    = module.acm.acm_certificate_arn
  api_mapping_key = local.stage_name_poc2

}
