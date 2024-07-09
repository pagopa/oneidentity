## Import ##

import {
  to = module.iam.aws_iam_openid_connect_provider.github
  id = "arn:aws:iam::471112878885:oidc-provider/token.actions.githubusercontent.com"
}

import {
  to = module.iam.aws_iam_role.githubiac
  id = "GitHubActionIACRole"
}


import {
  to = module.iam.aws_iam_role_policy_attachment.githubiac
  id = "GitHubActionIACRole/arn:aws:iam::aws:policy/AdministratorAccess"
}

import {
  to = module.backend.module.jwt_sign.aws_kms_key.this[0]
  id = "12ec0889-8be5-4719-ab6f-91e1bf017fc4"
}

import {
  to = module.backend.module.jwt_sign.aws_kms_alias.this["test-sign-jwt"]
  id = "alias/test-sign-jwt"
}

import {
  to = module.backend.aws_cloudwatch_log_group.ecs_core
  id = "/aws/ecs/oneid-es-1-d-core/oneid-core"
}

import {
  to = module.backend.aws_cloudwatch_log_group.ecs_spid_validator[0]
  id = "/aws/ecs/oneid-es-1-d-spid-validator/validator"
}


## Moved ##
moved {
  from = module.network.module.acm.aws_acm_certificate.this[0]
  to   = module.frontend.module.acm.aws_acm_certificate.this[0]
}

moved {
  from = module.alb.aws_vpc_security_group_ingress_rule.this["all_https"]
  to   = module.frontend.module.alb.aws_vpc_security_group_ingress_rule.this["all_https"]
}


moved {
  from = module.alb.aws_lb.this[0]
  to   = module.frontend.module.alb.aws_lb.this[0]
}

moved {
  from = module.alb.aws_lb_listener.this["ex-http-https-redirect"]
  to   = module.frontend.module.alb.aws_lb_listener.this["ex-http-https-redirect"]
}

moved {
  from = module.alb.aws_lb_listener.this["ex_https"]
  to   = module.frontend.module.alb.aws_lb_listener.this["ex_https"]
}

moved {
  from = module.alb.aws_lb_target_group.this["ecs_oneidentity"]
  to   = module.frontend.module.alb.aws_lb_target_group.this["ecs_oneidentity"]
}


moved {
  from = module.alb.aws_security_group.this[0]
  to   = module.frontend.module.alb.aws_security_group.this[0]
}

moved {
  from = module.alb.aws_vpc_security_group_egress_rule.this["all"]
  to   = module.frontend.module.alb.aws_vpc_security_group_egress_rule.this["all"]
}

moved {
  from = module.alb.aws_vpc_security_group_ingress_rule.this["all_http"]
  to   = module.frontend.module.alb.aws_vpc_security_group_ingress_rule.this["all_http"]
}
