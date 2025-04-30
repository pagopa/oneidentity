"""Lambda function which serves as BE for Client Portal"""

import boto3
import logging
import os
from typing import Optional
from aws_lambda_powertools import Tracer
from aws_lambda_powertools.event_handler import APIGatewayRestResolver
from aws_lambda_powertools.utilities.typing import LambdaContext
from localized_content_map import LocalizedContentMap


# ENVIRONMENT VARIABLES

# AWS_REGION
# USER_POOL_ID
# LOG_LEVEL
# CLIENT_REGISTRATIONS_TABLE_NAME

# Get tracer
tracer = Tracer()

# Get logger instance
logger = logging.getLogger()
logger.setLevel(os.getenv("LOG_LEVEL", "INFO"))

# Get APIGatewayResolver
app = APIGatewayRestResolver()

aws_region = os.getenv("AWS_REGION")


def check_client_id_exists(client_id: str) -> bool:
    """
    Check if client_id exists in ClientRegistrations table
    """
    try:
        response = dynamodb_client.get_item(
            TableName=os.getenv("CLIENT_REGISTRATIONS_TABLE_NAME"),
            Key={"clientId": {"S": client_id}},
        )
        logger.debug("[check_client_id_exists]: %s", response)
        return "Item" in response
    except Exception as ex:
        logger.error("[check_client_id_exists]: %s", repr(ex))
        return False

def get_cognito_client(region: str) -> Optional["boto3.client.cognito-idp"]:
    """
    Retrieve cognito client
    """
    try:
        client = boto3.client("cognito-idp", region_name=region)
        logger.debug("[get_cognito_client]: client up")
        return client
    except Exception as ex:
        logger.error("[get_cognito_client]: %s", repr(ex))
        return None

def get_dynamodb_client(region: str) -> Optional["boto3.client.DynamoDB"]:
    """
    Retrieve dynamodb client
    """
    try:
        client = boto3.client("dynamodb", region_name=region)
        logger.debug("[get_dynamodb_client]: client up")
        return client
    except Exception as ex:
        logger.error("[get_dynamodb_client]: %s", repr(ex))
        return None


# Cognito client
cognito_client = get_cognito_client(aws_region)
# DynamoDB client
dynamodb_client = get_dynamodb_client(aws_region)


@app.put("/admin/client-manager/user-attributes")
def update_user_attributes_with_client_id():
    """
    Inserts 'client_id' inside Cognito User Attributes
    """
    logger.info("/admin/client-manager/user-attributes route invoked")
    try:
        # Parse the JSON body of the request
        body = app.current_event.json_body

        # Validate the body (optional)
        if not body:
            return {"message": "Request body is required"}, 400

        # Extract the client_id from the request body
        client_id = body.get("client_id")
        # Extract the user_id from the request body
        user_id = body.get("user_id")

        if not client_id or not user_id:
            return {"message": "client_id and user_id are required"}, 400
        
        # Check if client_id exists in ClientRegistrations table
        if not check_client_id_exists(client_id):
            return {"message": "client_id not found"}, 404

        # Update the user attributes in Cognito inserting the client_id
        response = cognito_client.admin_update_user_attributes(
            UserPoolId=os.getenv("USER_POOL_ID"),
            Username=user_id,
            UserAttributes=[{"Name": "custom:client_id", "Value": client_id}],
        )
        logger.debug("[update_user_attributes_with_client_id]: %s", response)
        # Check if the response indicates success
        if response.get("ResponseMetadata", {}).get("HTTPStatusCode") != 200:
            logger.error("[update_user_attributes_with_client_id]: %s", response)
            return {"message": "Failed to update user attributes"}, 500

        # Return success response
        return {"message": "Optional attributes updated successfully"}, 200

    except Exception as e:
        logger.error("Error updating client id: %s", repr(e))
        return {"message": "Internal server error"}, 500
    return None


@app.put("/admin/client-manager/client-additional/<client_id>")
def update_optional_attributes(client_id):
    """
    Updates optional fields on 'ClientRegistrations' Table
    """
    logger.info("/admin/client-manager/client-additional route invoked")
    try:
        # Parse the JSON body of the request
        body = app.current_event.json_body

        # Validate the body (optional)
        if not body:
            return {"message": "Request body is required"}, 400
        
        # Check if client_id exists in ClientRegistrations table
        if not check_client_id_exists(client_id):
            return {"message": "client_id not found"}, 404

        # TODO: check if client_id is associated with the Cognito user_id which executed the request

        # Extract optional attributes from the request body
        a11y_uri = body.get("a11y_uri")
        back_button_enabled = body.get("back_button_enabled")
        localized_content = body.get("localizedContentMap")

        localized_content_map_object = LocalizedContentMap.from_json(localized_content)
        logger.debug("[update_optional_attributes]: %s", localized_content_map_object)
        localized_content_map_object_value = localized_content_map_object.to_dynamodb()
        logger.debug("[update_optional_attributes]: %s", localized_content_map_object_value)
        # Update the optional attributes in DynamoDB in ClientRegistrations table
        response = dynamodb_client.update_item(
            TableName=os.getenv("CLIENT_REGISTRATIONS_TABLE_NAME"),
            Key={"clientId": {"S": client_id}},
            UpdateExpression="SET a11y_uri = :a11y_uri, back_button_enabled = :back_button_enabled, localizedContentMap = :localizedContentMap",
            ExpressionAttributeValues={
                ":a11y_uri": {"S": a11y_uri},
                ":back_button_enabled": {"BOOL": back_button_enabled},
                ":localizedContentMap": localized_content_map_object_value,
            },
        )
        logger.debug("[update_optional_attributes]: %s", response)

        # Check if the response indicates success
        if response.get("ResponseMetadata", {}).get("HTTPStatusCode") != 200:
            logger.error("[update_optional_attributes]: %s", response)
            return {"message": "Failed to update optional attributes"}, 500

    except Exception as e:
        logger.error("Error updating optional attributes: %s", repr(e))
        return {"message": "Internal server error"}, 500
    return None


@tracer.capture_lambda_handler
def handler(event: dict, context: LambdaContext) -> dict:
    """
    Lambda handler that call routes of aws_lambda_powertools
    """
    return app.resolve(event, context)
