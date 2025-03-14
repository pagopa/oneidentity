def lambda_handler(event, context):
    email = event['request']['userAttributes']['email']
    if not email.endswith('@pagopa.it'):
        raise Exception("Invalid email domain. Only @pagopa.it is allowed.")
    return event