locals {
  web_acl_rules = [
    {
      name                    = "IpReputationList"
      priority                = 1
      managed_rule_group_name = "AWSManagedRulesAmazonIpReputationList"
      vendor_name             = "AWS"
      metric_name             = "IpReputationList"
    },
    {
      name                    = "CommonRuleSet"
      priority                = 2
      managed_rule_group_name = "AWSManagedRulesCommonRuleSet"
      vendor_name             = "AWS"
      metric_name             = "CommonRuleSet"
    },
    {
      name                    = "KnownBadInputsRuleSet"
      priority                = 3
      managed_rule_group_name = "AWSManagedRulesKnownBadInputsRuleSet"
      vendor_name             = "AWS"
      metric_name             = "KnownBadInputsRuleSet"
    },
    {
      name                    = "SQLiRuleSet"
      priority                = 4
      managed_rule_group_name = "AWSManagedRulesSQLiRuleSet"
      vendor_name             = "AWS"
      metric_name             = "SQLiRuleSet"
    }
  ]
  #  acm_records = can(module.auth_admin[0].acm_certificate_domain_validation_options) ? [for record in module.auth_admin[0].acm_certificate_domain_validation_options : {
  #       name    = record.resource_record_name
  #       type    = record.resource_record_type
  #       ttl     = 60
  #       records = [record.resource_record_value]
  #     }]: []

}
