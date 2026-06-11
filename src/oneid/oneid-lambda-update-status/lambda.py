"""
Lambda update status
"""

from concurrent.futures import ThreadPoolExecutor
import json
import logging
import os
import time

import boto3

# AWS region
AWS_REGION = os.getenv("AWS_REGION")

# S3 bucket name for storing assets
ASSETS_S3_BUCKET = os.getenv("ASSETS_S3_BUCKET")

# DynamoDB table name for IDP metadata
IDP_METADATA_DYNAMODB_TABLE = os.getenv("IDP_METADATA_DYNAMODB_TABLE")

# IDP metadata pointer used by /idps latest snapshot
IDP_METADATA_POINTER = "LATEST_SPID"

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
    "IDPNoTrafficErrorRateAlarm": {
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
    "ClientNoTrafficErrorRateAlarm": {
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
logger.setLevel(os.getenv("LOG_LEVEL", "INFO"))

# Initialize a boto3 client for DynamoDB
dynamodb_client = boto3.client("dynamodb", region_name=AWS_REGION)

# Initialize a boto3 client for S3
s3_client = boto3.client("s3", region_name=AWS_REGION)


def get_event_data(event):
    """
    Extract the event data from event
    """
    # Extract the event data considering that it is a CloudWatch alarm event
    # The alarm name is in the format {ALARM_TYPE}_{ENV_SHORT}_{KEY}
    # ALARM_TYPE is one of the following: IDPErrorRateAlarm, ClientErrorRateAlarm
    # whilst KEY is "entityId" if Alarm Type is "IDPErrorRateAlarm" or "FriendlyName_ClientId" if Alarm Type is "ClientErrorRateAlarm"
    alarm_data = event["alarmData"]
    alarm_name = alarm_data["alarmName"]
    alarm_type, env_short, key = alarm_name.split("_", 2)
    if alarm_type == "ClientErrorRateAlarm" or alarm_type == "ClientNoTrafficErrorRateAlarm":
        # Extract the client ID from the alarm name
        # The client ID is the last 43 characters of the alarm name
        key = key[-43:]
    alarm_state = alarm_data["state"]["value"]

    return alarm_type, key, alarm_state


def update_status(alarm_type, key, alarm_state) -> bool:
    """
    Update the key status in the DynamoDB table
    """
    # Update the KEY status in the DynamoDB table
    old_item = None
    # Get the current Unix timestamp as a string
    current_timestamp = str(int(time.time()))

    new_status = ALARM_STATE_MAP[alarm_state]

    status_attribute = IDS_MAP[alarm_type]["statusId"]
    key_attribute = IDS_MAP[alarm_type]["keyId"]

    # Remove the latest key status
    try:
        response = dynamodb_client.delete_item(
            TableName=IDS_MAP[alarm_type]["tableName"],
            Key={key_attribute: {"S": key}, "pointer": {"S": LATEST_POINTER}},
            ConditionExpression=f"{status_attribute} <> :status_value",
            ExpressionAttributeValues={":status_value": {"S": new_status}},
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


def update_idp_metadata_status(entity_id: str, alarm_state: str) -> bool:
    """
    Update the status field in IDPMetadata table for the affected entity.
    """
    if not IDP_METADATA_DYNAMODB_TABLE:
        logger.error("Missing IDP_METADATA_DYNAMODB_TABLE environment variable")
        return False

    new_status = ALARM_STATE_MAP[alarm_state]

    try:
        dynamodb_client.update_item(
            TableName=IDP_METADATA_DYNAMODB_TABLE,
            Key={"entityID": {"S": entity_id}, "pointer": {"S": IDP_METADATA_POINTER}},
            UpdateExpression="SET #status = :status",
            ExpressionAttributeNames={"#status": "status"},
            ExpressionAttributeValues={":status": {"S": new_status}},
            ConditionExpression="attribute_exists(entityID) AND attribute_exists(pointer)",
        )
        logger.info("Updated IDPMetadata status for entityID %s to %s", entity_id, new_status)
        return True
    except dynamodb_client.exceptions.ConditionalCheckFailedException:
        logger.error(
            "IDPMetadata item not found for entityID %s and pointer %s",
            entity_id,
            IDP_METADATA_POINTER,
        )
        return False
    except Exception as e:
        logger.error("Error updating IDPMetadata for entityID %s: %s", entity_id, e)
        return False


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

    logger.info("Processing %s alarm for key %s", alarm_type, key)

    with ThreadPoolExecutor(max_workers=2) as executor:
        futures = [executor.submit(update_status, alarm_type, key, alarm_state)]

        if IDS_MAP[alarm_type]["type"] == "IDP":
            futures.append(executor.submit(update_idp_metadata_status, key, alarm_state))

        update_results = [future.result() for future in futures]

    if not all(update_results):
        logger.error("Error updating status for alarm type %s", alarm_type)
        return {"statusCode": 500, "body": json.dumps(f"Error updating {IDS_MAP[alarm_type]} status")}

    # Update the S3 asset file with latest status info

    # Get the latest status from DynamoDB
    latest_status = get_all_latest_status(alarm_type)

    if update_s3_asset_file(alarm_type, latest_status):
        return {
            "statusCode": 200,
            "body": json.dumps("Status updated successfully"),
        }

    return {"statusCode": 500, "body": json.dumps("Error updating S3 asset file")}
