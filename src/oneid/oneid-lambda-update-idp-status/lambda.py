"""
Lambda update idp status
"""

import json
import logging
import os
import time

import boto3

AWS_REGION = os.getenv("AWS_REGION")
IDP_STATUS_DYNAMODB_TABLE = os.getenv("IDP_STATUS_DYNAMODB_TABLE")
ASSETS_S3_BUCKET = os.getenv("ASSETS_S3_BUCKET")
IDP_STATUS_S3_FILE_NAME = os.getenv("IDP_STATUS_S3_FILE_NAME")

LATEST_POINTER = "latest"
IDP_ERROR_RATE_ALARM = "IDPErrorRateAlarm"
IDP_STATUS_OK = "OK"
IDP_STATUS_KO = "KO"
ALARM_STATE_MAP = {"ALARM": "KO", "OK": "OK"}

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


def update_idp_status(idp, alarm_state) -> bool:
    """
    Update the IDP status in the DynamoDB table
    """
    # Update the IDP status in the DynamoDB table
    old_item = None
    # Get the current Unix timestamp as a string
    current_timestamp = str(int(time.time()))

    new_status = ALARM_STATE_MAP[alarm_state]

    # Remove the latest IDP status
    try:
        response = dynamodb_client.delete_item(
            TableName=IDP_STATUS_DYNAMODB_TABLE,
            Key={"entityID": {"S": idp}, "pointer": {"S": LATEST_POINTER}},
            ConditionExpression="idpStatus <> :idpStatus",
            ExpressionAttributeValues={":idpStatus": {"S": new_status}},
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

    old_status = old_item["idpStatus"]["S"]

    # Add new entry with unix timestamp as the range key and old status as the status
    try:
        dynamodb_client.put_item(
            TableName=IDP_STATUS_DYNAMODB_TABLE,
            Item={
                "entityID": {"S": idp},
                "pointer": {"S": current_timestamp},
                "idpStatus": {"S": old_status},
            },
        )
    except Exception as e:
        logger.error("Error inserting item: %s", e)
        return False

    # Add new latest entry with the new status
    try:
        dynamodb_client.put_item(
            TableName=IDP_STATUS_DYNAMODB_TABLE,
            Item={
                "entityID": {"S": idp},
                "pointer": {"S": LATEST_POINTER},
                "idpStatus": {"S": new_status},
            },
        )
    except Exception as e:
        logger.error("Error inserting item: %s", e)
        return False

    logger.info("Updated IDP status: %s, from %s to %s", idp, old_status, new_status)
    return True


def get_all_latest_status():
    """
    Get all items with {LATEST_POINTER} as the sort key from DynamoDB
    """
    try:
        response = dynamodb_client.scan(
            TableName=IDP_STATUS_DYNAMODB_TABLE,
            FilterExpression="pointer = :pointer",
            ExpressionAttributeValues={":pointer": {"S": LATEST_POINTER}},
        )
        return response.get("Items", [])
    except Exception as e:
        logger.error("Error scanning items: %s", e)
        return []


def update_s3_asset_file(idp_latest_status) -> bool:
    """
    Update the S3 asset file with the latest IDP status
    """
    idp_status_list = [
        {"IDP": idp["entityID"]["S"], "Status": idp["idpStatus"]["S"]}
        for idp in idp_latest_status
    ]

    # Convert the list to JSON
    idp_status_json = json.dumps(idp_status_list)

    # Upload the JSON file to S3
    try:
        s3_client.put_object(
            Bucket=ASSETS_S3_BUCKET,
            Key=IDP_STATUS_S3_FILE_NAME,
            Body=idp_status_json,
            ContentType="application/json",
        )
        logger.info(
            "Uploaded IDP status to S3: %s/%s",
            ASSETS_S3_BUCKET,
            IDP_STATUS_S3_FILE_NAME,
        )
    except Exception as e:
        logger.error("Error uploading IDP status to S3: %s", e)
        return False

    return True


def lambda_handler(event, context):
    """
    Lambda handler
    """
    logger.info("Received event: %s", json.dumps(event))
    # Extract the event type, the idp and state
    alarm_type, idp, alarm_state = get_event_data(event)

    if alarm_type == IDP_ERROR_RATE_ALARM:
        # Update the IDP status using {alarm_state}
        if not update_idp_status(idp, alarm_state):
            logger.error("Error updating IDP status")
            return {"statusCode": 500, "body": json.dumps("Error updating IDP status")}
    else:
        logger.error("Invalid alarm type: %s", alarm_type)
        return {"statusCode": 400, "body": json.dumps("Invalid alarm type")}

    # Update the S3 asset file with latest IDP status

    # Get the latest IDP status from DynamoDB
    idp_latest_status = get_all_latest_status()

    if update_s3_asset_file(idp_latest_status):
        return {
            "statusCode": 200,
            "body": json.dumps("IDP status updated successfully"),
        }

    return {"statusCode": 500, "body": json.dumps("Error updating S3 asset file")}
