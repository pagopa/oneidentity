"""
Lambda update idp status
"""

import json
import logging
import os
import time

import boto3

# AWS region
AWS_REGION = os.getenv("AWS_REGION")

# S3 bucket name for storing assets
ASSETS_S3_BUCKET = os.getenv("ASSETS_S3_BUCKET")

# Pointer value for the latest status
LATEST_POINTER = "latest"

# Mapping of alarm states to status values
ALARM_STATE_MAP = {"ALARM": "KO", "OK": "OK"}

IDS_MAP = {
    "IDPErrorRateAlarm": {
        "type": "IDP",
        "keyId": "entityID",
        "statusId": "idpStatus",
        "tableName": os.getenv("IDP_STATUS_DYNAMODB_TABLE"),
        "idxName": os.getenv("IDP_STATUS_DYNAMODB_IDX"),
        "fileName": os.getenv("IDP_STATUS_S3_FILE_NAME")
    },
    "ClientErrorRateAlarm": {
        "type": "Client",
        "keyId": "clientID",
        "statusId": "clientStatus",
        "tableName": os.getenv("CLIENT_STATUS_DYNAMODB_TABLE"),
        "idxName": os.getenv("CLIENT_STATUS_DYNAMODB_IDX"),
        "fileName": os.getenv("CLIENT_STATUS_S3_FILE_NAME")
    },
}

# Initialize a logger
logger = logging.getLogger()
logger.setLevel(os.getenv("LOG_LEVEL", "DEBUG"))

# Initialize a boto3 client for DynamoDB
dynamodb_client = boto3.client("dynamodb", region_name=AWS_REGION)

# Initialize a boto3 client for S3
s3_client = boto3.client("s3", region_name=AWS_REGION)


def get_event_data(event):
    """
    Extract the event data from event
    """
    # Extract the event data considering that it is a CloudWatch alarm event
    # The alarm name is in the format {ALARM_TYPE}-{IDP}
    # ALARM_TYPE is one of the following: IDPErrorAlarm, IDPSuccessAlarm
    # whilst IDP is one of the IDP entity ids
    alarm_data = event["alarmData"]
    alarm_name = alarm_data["alarmName"]
    alarm_type, idp = alarm_name.split("-")
    alarm_state = alarm_data["state"]["value"]

    return alarm_type, idp, alarm_state


def update_status(alarm_type, key, alarm_state) -> bool:
    """
    Update the key status in the DynamoDB table
    """
    # Update the KEY status in the DynamoDB table
    old_item = None
    # Get the current Unix timestamp as a string
    current_timestamp = str(int(time.time()))

    new_status = ALARM_STATE_MAP[alarm_state]

    # Remove the latest key status
    try:
        response = dynamodb_client.delete_item(
            TableName=IDS_MAP[alarm_type]["tableName"],
            Key={f"{IDS_MAP[alarm_type]["keyId"]}": {"S": key}, "pointer": {"S": LATEST_POINTER}},
            ConditionExpression=f"{IDS_MAP[alarm_type]["statusId"]} <> :{IDS_MAP[alarm_type]["statusId"]}",
            ExpressionAttributeValues={f":{IDS_MAP[alarm_type]["statusId"]}": {"S": new_status}},
            ReturnValues="ALL_OLD",
        )
        old_item = response["Attributes"]
        logger.info("Deleted item: %s", response)
    # If the status is already the new status, do not delete the item
    except dynamodb_client.exceptions.ConditionalCheckFailedException:
        logger.info("No item deleted as the status is already %s", new_status)
        return True
    except KeyError as e:
        logger.error("Error deleting item: %s", e)
        return False

    old_status = old_item[IDS_MAP[alarm_type]["statusId"]]["S"]

    # Add new entry with unix timestamp as the range key and old status as the status
    try:
        dynamodb_client.put_item(
            TableName=IDS_MAP[alarm_type]["tableName"],
            Item={
                IDS_MAP[alarm_type]["keyId"]: {"S": key},
                "pointer": {"S": current_timestamp},
                IDS_MAP[alarm_type]["statusId"]: {"S": old_status},
            },
        )
    except Exception as e:
        logger.error("Error inserting item: %s", e)
        return False

    # Add new latest entry with the new status
    try:
        dynamodb_client.put_item(
            TableName=IDS_MAP[alarm_type]["tableName"],
            Item={
                IDS_MAP[alarm_type]["keyId"]: {"S": key},
                "pointer": {"S": LATEST_POINTER},
                IDS_MAP[alarm_type]["statusId"]: {"S": new_status},
            },
        )
    except Exception as e:
        logger.error("Error inserting item: %s", e)
        return False

    logger.info("Updated key status: %s, from %s to %s", key, old_status, new_status)
    return True


def get_all_latest_status(alarm_type):
    """
    Get all items with {LATEST_POINTER} as the sort key from DynamoDB
    """
    try:
        response = dynamodb_client.query(
            TableName=IDS_MAP[alarm_type]["tableName"],
            IndexName=IDS_MAP[alarm_type]["idxName"],
            KeyConditionExpression="pointer = :pointer",
            ExpressionAttributeValues={":pointer": {"S": LATEST_POINTER}},
        )
        return response.get("Items", [])
    except Exception as e:
        logger.error("Error scanning items: %s", e)
        return []


def update_s3_asset_file(alarm_type, latest_status) -> bool:
    """
    Update the S3 asset file with the latest status
    """
    status_list = [
        {IDS_MAP[alarm_type]["type"]: key[IDS_MAP[alarm_type]["keyId"]]["S"], "Status": key[IDS_MAP[alarm_type]["statusId"]]["S"]}
        for key in latest_status
    ]

    # Convert the list to JSON
    status_json = json.dumps(status_list)

    # Upload the JSON file to S3
    try:
        s3_client.put_object(
            Bucket=ASSETS_S3_BUCKET,
            Key=IDS_MAP[alarm_type]["fileName"],
            Body=status_json,
            ContentType="application/json",
        )
        logger.info(
            "Uploaded %s status to S3: %s/%s",
            IDS_MAP[alarm_type]["type"],
            ASSETS_S3_BUCKET,
            IDS_MAP[alarm_type]["fileName"],
        )
    except Exception as e:
        logger.error("Error uploading %s status to S3: %s", IDS_MAP[alarm_type]["type"], e)
        return False

    return True


def lambda_handler(event, context):
    """
    Lambda handler
    """
    logger.info("Received event: %s", json.dumps(event))

    # Extract the event type, the key and state
    alarm_type, key, alarm_state = get_event_data(event)

    if alarm_type not in IDS_MAP:
        logger.error("Invalid alarm type: %s", alarm_type)
        return {"statusCode": 400, "body": json.dumps("Invalid alarm type")}

    if not update_status(alarm_type, key, alarm_state):
        logger.error("Error updating %s status",IDS_MAP[alarm_type])
        return {"statusCode": 500, "body": json.dumps(f"Error updating {IDS_MAP[alarm_type]} status")}

    # Update the S3 asset file with latest status info

    # Get the latest status from DynamoDB
    latest_status = get_all_latest_status(alarm_type)

    if update_s3_asset_file(alarm_type,latest_status):
        return {
            "statusCode": 200,
            "body": json.dumps("Status updated successfully"),
        }

    return {"statusCode": 500, "body": json.dumps("Error updating S3 asset file")}
