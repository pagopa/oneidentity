"""
This Lambda function flushes the cache of a specific stage in an API Gateway REST API.
"""

import boto3
import os
import logging

logger = logging.getLogger()
logger.setLevel(logging.INFO)

api_gateway_client = boto3.client('apigateway')

REST_API_ID = os.environ.get("REST_API_ID")
STAGE_NAME = os.environ.get("STAGE_NAME")

def lambda_handler(event, context):
  """
  Lambda function handler to flush the cache of a specific stage in an API Gateway REST API.
  """

  try:
    api_gateway_client.flush_stage_cache(
    restApiId=REST_API_ID,
    stageName=STAGE_NAME,
    )
    logger.info("Stage cache flushed successfully.")
    return True
  except Exception as ex:
    logger.error("Error during flush stage cache operation: %s", repr(ex))
    return False
