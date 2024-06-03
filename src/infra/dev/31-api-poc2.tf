/*

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

  body = templatefile("./api/poc2.tpl.json",
    {
      server_url    = var.r53_dns_zone.name
      uri           = format("http://%s:%s", module.elb.dns_name, local.container_poc2_port),
      connection_id = aws_api_gateway_vpc_link.apigw.id
  })



  custom_domain_name        = var.r53_dns_zone.name
  create_custom_domain_name = false
  #certificate_arn    = module.acm.acm_certificate_arn
  api_mapping_key = null

}


*/