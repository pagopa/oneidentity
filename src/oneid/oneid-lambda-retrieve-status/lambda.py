"""
Lambda function for the retrieve status endpoint
"""

import json
import logging
import os
from collections import defaultdict

import boto3

# Get logger instance
logger = logging.getLogger()
logger.setLevel(os.getenv("LOG_LEVEL", "INFO"))

# AWS region
AWS_REGION = os.getenv("AWS_REGION")

# Get dynamodb client instance
dynamodb = boto3.client("dynamodb", AWS_REGION)

# Define 'latest' pointer
LATEST_POINTER = 'latest'

# Get the tables name from the environment

EVENT_TYPE_MAPPER = {
    "idp": {
        "table_name": os.getenv("DYNAMODB_IDP_STATUS_HISTORY_TABLE_NAME"),
        "key_id": "entityID",
        "status_id": "idpStatus",
    },
    "client": {
        "table_name": os.getenv("DYNAMODB_CLIENT_STATUS_HISTORY_TABLE_NAME"),
        "key_id": "clientID",
        "status_id": "clientStatus",
    },
}

def validate_input_data(start, end):
    """
    Validate start and end parameters:
    - If end is provided, start is required
    - Check if start and end timestamp format is valid
    - Check if start is less than end
    """
    if end and not start:
        raise ValueError("Start date is required when end date is provided")

    for timestamp in [start, end]:
        if timestamp and not check_timestamp_format(timestamp):
            raise ValueError(f"Invalid timestamp format: {timestamp}")

    if start and end:
        start_value = int(start) if start != LATEST_POINTER else float("inf")
        end_value = int(end) if end != LATEST_POINTER else float("inf")

        if start_value > end_value:
            raise ValueError("Start date cannot be greater than end date")

def check_timestamp_format(timestamp) -> bool:
    """
    Check if timestamp format is a valid unix timestamp
    """
    if timestamp == LATEST_POINTER:
        return True
    try:
        int(timestamp)
    except ValueError:
        logger.error("Invalid timestamp format")
        return False
    return True

def get_event_data(event):
    """
    Get the needed data from the event
    """
    # Get the data from the event
    # The event is an API Gateway event in a lambda proxy integration
    # The endpoint in in the form of /{type}/status
    ## with 'entity_key', 'start' and 'end' passed as query parameters

    event_type = event["pathParameters"]["type"]
    entity_key = event["queryStringParameters"].get("entityKey", "").strip()
    start = event["queryStringParameters"].get("start", "").strip()
    end = event["queryStringParameters"].get("end", "").strip()

    return event_type, entity_key, start, end

def build_filtered_output(status_id, start, end, items):
    """
    Build the filtered output
    """
    values = {}

    for item in items:
        if item["pointer"]["S"] == LATEST_POINTER:
            values[float("inf")] = item[status_id]["S"]
        else:
            values[int(item["pointer"]["S"])] = item[status_id]["S"]

    # Filter the items by the timestamp, that must be between start and end
    filtered_items = {str(timestamp) if timestamp != float("inf") else LATEST_POINTER : value for timestamp, value in values.items() if start <= timestamp <= end}

    return filtered_items

def get_status_history(event_type, entity_key, start, end):
    """
    Retrieve the status history from dynamodb
    """
    # Get the table name, key id and status id from the event type
    table_name = EVENT_TYPE_MAPPER[event_type]["table_name"]
    key_id = EVENT_TYPE_MAPPER[event_type]["key_id"]
    status_id = EVENT_TYPE_MAPPER[event_type]["status_id"]

    if start:
        start = float(start) if start != "latest" else float("inf")
    else:
        start = float("-inf")
    if end:
        end = float(end) if end != "latest" else float("inf")
    else:
        end = float("inf")

    if entity_key:

        # Get all values for the entity key
        response = dynamodb.query(
            TableName=table_name,
            KeyConditionExpression=f"{key_id} = :key",
            ExpressionAttributeValues={":key": {"S": entity_key}},
        )

        # Get the items from the response
        items = response.get("Items", [])
        if not items:
            return None

        # Filter items based on start and end dates
        result = build_filtered_output(status_id, start, end, items)
        if result:
            return { entity_key : result }
        return None
    # Get all values
    response = dynamodb.scan(TableName=table_name)

    # Get the items from the response
    items = response.get("Items", [])
    if not items:
        return None

    # Filter items based on start and end dates

    values = defaultdict(list)

    for item in items:
        values[item[key_id]["S"]].append(item)

    output_items = {}

    for key, value in values.items():
        result = build_filtered_output(status_id, start, end, value)
        if result:
            output_items[key] = result

    return output_items

def lambda_handler(event, context):
    """
    Lambda handler
    """
    logger.info("Received event: %s", json.dumps(event))

    # Get event data
    event_type, entity_key, start, end = get_event_data(event)

    # Validate input data
    try:
        validate_input_data(start, end)
    except ValueError as error:
        return {"statusCode": 400, "body": str(error)}

    # Retrieve data from dynamodb

    result = get_status_history(event_type, entity_key, start, end)

    if not result:
        return {"statusCode": 404, "body": json.dumps("Not found")}

    return {"statusCode": 200, "body": result}
