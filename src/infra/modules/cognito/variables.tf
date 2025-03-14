variable "cognito" {
  type = object({
    user_pool_name   = string,
    user_pool_domain = string,
    user_pool_client = string,
    logout_url       = string, #https://dev.oneid.pagopa.it/logout
    callback_url     = string
  })
}

variable "cognito_presignup_lambda" {
  type = object({
    name                              = string
    filename                          = string
    cloudwatch_logs_retention_in_days = number
  })
}
