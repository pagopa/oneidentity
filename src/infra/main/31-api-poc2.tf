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
    openapi = "3.0.3"
    info = {
      title   = "PocV2"
      version = "1.0"
    }
    servers = [
      {
        url = format("%s{basePath}", module.elb.dns_name)
        "variables" : {
          "basePath" : {
            "default" : "/v2"
          }
        }
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
      "/saml/metadata" = {
        get = {
          x-amazon-apigateway-integration = {
            type                = "HTTP"
            httpMethod          = "GET"
            uri                 = format("http://%s:%s/saml/metadata", module.elb.dns_name, local.container_poc2_port)
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
      "/hello" = {
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
            uri            = format("http://%s:%s/hello", module.elb.dns_name, local.container_poc2_port),
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
                  "method.response.header.Cookie"       = "integration.response.header.Cookie"
                }
              }
              302 = {
                statusCode = "302",
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

            302 = {
              "$ref" = "#/components/responses/Redirect"
            }
          }
        }
      }
      "/saml/acs" = {
        post = {
          "requestBody" : {
            "required" : true,
            "content" : {
              "text/html" : {
                "schema" : {
                  "type" : "object",
                  "properties" : {
                    "RelayState" : {
                      "type" : "string",
                      "description" : "The relay state associated with the SAML request"
                    },
                    "SAMLResponse" : {
                      "type" : "string",
                      "description" : "The SAML response received from the identity provider"
                    }
                  },
                  "required" : ["RelayState", "SAMLResponse"]
                }
              }
            }
          }

          x-amazon-apigateway-integration = {
            type           = "HTTP"
            httpMethod     = "POST"
            uri            = format("http://%s:%s/saml/acs", module.elb.dns_name, local.container_poc2_port),
            connectionType = "VPC_LINK"
            connectionId   = aws_api_gateway_vpc_link.apigw.id
            requestParameters = {
              "integration.request.body.RelayState"   = "method.request.body.RelayState"
              "integration.request.body.SAMLResponse" = "method.request.body.SAMLResponse"
            }
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
          headers = {
            Content-Type = {
              schema = {
                type = "string"
              }
            }
          }
        }
        Redirect = {
          description = "302 redirect"
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


  custom_domain_name        = var.r53_dns_zone.name
  create_custom_domain_name = false
  #certificate_arn    = module.acm.acm_certificate_arn
  api_mapping_key = null

}
