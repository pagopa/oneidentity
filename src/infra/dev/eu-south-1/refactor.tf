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
  to = module.backend.aws_cloudwatch_log_group.ecs_core
  id = "/aws/ecs/oneid-es-1-d-core/oneid-core"
}

import {
  to = module.storage.aws_iam_role_policy_attachment.glue_s3_assertions_policy[0]
  id = "AWSGlueServiceRole-Assertions/arn:aws:iam::aws:policy/service-role/AWSGlueServiceRole"
}

import {
  to = module.storage.aws_iam_role_policy_attachment.glue_s3_assertions_policy[1]
  id = "AWSGlueServiceRole-Assertions/arn:aws:iam::471112878885:policy/AWSGlueServiceRoleAssertionsS3Policy"
}

import {
  to = module.backend.aws_ssm_parameter.key_pem
  id = "key.pem"
}


import {
  to = module.frontend.module.rest_api.aws_api_gateway_authorizer.main[0]
  id = "l3ji2mvg0f/umuwue"
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

moved {
  from = module.backend.module.ecs
  to   = module.backend.module.ecs_cluster
}


## Refactor spid-validator module

moved {
  from = module.backend.module.ecs_spid_validator[0]
  to   = module.spid_validator.module.ecs_spid_validator
}

moved {
  from = module.backend.module.ecr["oneid-es-1-d-spid-validator"]
  to   = module.spid_validator.module.ecr
}

moved {
  from = module.frontend.module.alb_spid_validator[0]
  to   = module.spid_validator.module.alb
}

moved {
  from = module.frontend.module.acm_validator[0]
  to   = module.spid_validator.module.acm_validator
}

moved {
  from = module.backend.aws_cloudwatch_log_group.ecs_spid_validator[0]
  to   = module.spid_validator.aws_cloudwatch_log_group.ecs_spid_validator
}


moved {
  from = module.frontend.module.records.aws_route53_record.this["validator A"]
  to   = module.spid_validator.module.record.aws_route53_record.this["validator A"]
}

moved {
  from = module.backend.aws_iam_role.pipe_sessions[0]
  to   = module.backend.aws_iam_role.pipe_sessions
}

moved {
  from = module.backend.aws_iam_role_policy.pipe_source[0]
  to   = module.backend.aws_iam_role_policy.pipe_source
}

moved {
  from = module.backend.aws_pipes_pipe.sessions[0]
  to   = module.backend.aws_pipes_pipe.sessions
}

moved {
  from = module.backend.aws_sqs_queue.dlq_lambda_assertion[0]
  to   = module.backend.aws_sqs_queue.dlq_lambda_assertion
}

moved {
  from = module.backend.aws_sqs_queue.pipe_dlq[0]
  to   = module.backend.aws_sqs_queue.pipe_dlq
}

moved {
  from = module.backend.module.assertion_lambda[0]
  to   = module.backend.module.assertion_lambda
}

moved {
  from = module.frontend.module.zones.aws_route53_zone.this["dev.oneid.pagopa.it"]
  to   = module.r53_zones.module.r53_zones.aws_route53_zone.this["dev.oneid.pagopa.it"]
}

moved {
  from = module.storage.module.s3_athena_output_bucket
  to   = module.storage.module.s3_athena_output_bucket[0]
}

moved {
  from = module.database.module.dynamodb_table_idpMetadata
  to   = module.database.module.dynamodb_table_idpMetadata[0]
}

moved {
  from = module.database.module.dynamodb_table_client_registrations
  to   = module.database.module.dynamodb_table_client_registrations[0]
}

moved {
  from = module.frontend.module.records.aws_route53_record.this[" A"]
  to   = module.frontend.module.records[0].aws_route53_record.this[" A"]
}

moved {
  from = module.storage.module.s3_assets_bucket
  to   = module.storage.module.s3_assets_bucket[0]
}

moved {
  from = module.storage.aws_iam_policy.github_s3_policy
  to   = module.storage.aws_iam_policy.github_s3_policy[0]
}

moved {
  from = module.storage.module.s3_idp_metadata_bucket
  to   = module.storage.module.s3_idp_metadata_bucket[0]
}

moved {
  from = module.frontend.module.rest_api_admin
  to   = module.frontend.module.rest_api_admin[0]
}