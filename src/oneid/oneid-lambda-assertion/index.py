import boto3
import logging

# See https://docs.aws.amazon.com/lambda/latest/dg/python-logging.html
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# logging.getLogger("boto3").setLevel(logging.DEBUG)
# logging.getLogger("botocore").setLevel(logging.DEBUG)

def lambda_handler(event, context):

    logger.info('Event %s', event)

    result = "Hello World"
    return {
        'statusCode' : 200,
        'body': result
    }