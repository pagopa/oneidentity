"""Lambda function which serves as BE for Client Portal"""

import logging
import os
from typing import Optional

import boto3
from aws_lambda_powertools import Tracer
from aws_lambda_powertools.event_handler import APIGatewayRestResolver
from aws_lambda_powertools.utilities.typing import LambdaContext
from localized_content_map import LocalizedContentMap

# ENVIRONMENT VARIABLES

# AWS_REGION
# USER_POOL_ID
# LOG_LEVEL
# CLIENT_REGISTRATIONS_TABLE_NAME
# IDP_INTERNAL_USERS_TABLE_NAME
# IDP_INTERNAL_USERS_GSI_NAME

# Get tracer
tracer = Tracer()

# Get logger instance
logger = logging.getLogger()
logger.setLevel(os.getenv("LOG_LEVEL", "INFO"))

# Get APIGatewayResolver
app = APIGatewayRestResolver()

aws_region = os.getenv("AWS_REGION")

valid_saml_attributes = set(
    [
        "spidCode",
        "name",
        "familyName",
        "placeOfBirth",
        "countyOfBirth",
        "dateOfBirth",
        "gender",
        "companyName",
        "registeredOffice",
        "fiscalNumber",
        "ivaCode",
        "idCard",
        "mobilePhone",
        "email",
        "address",
        "expirationDate",
        "digitalAddress",
        "domicileAddress",
        "domicilePlace",
        "domicilePostalCode",
        "domicileProvince",
        "domicileCountry",
        "qualification",
        "commonName",
        "surname",
        "givenName",
        "preferredUsername",
        "title",
        "userCertificate",
        "employeeNumber",
        "orgUnitName",
        "preferredLanguage",
        "country",
        "stateOrProvince",
        "city",
        "postalCode",
        "street",
    ]
)


def extract_client_id_from_connected_user(user_id: str) -> Optional[str]:
    """
    Extracts the client_id from the connected user using the user_id.
    """
    try:
        # Get the user attributes from Cognito
        response = cognito_client.admin_get_user(
            UserPoolId=os.getenv("USER_POOL_ID"),
            Username=user_id,
        )
        logger.debug("[extract_client_id_from_connected_user]: %s", response)

        # Find the custom:client_id attribute
        for attr in response.get("UserAttributes", []):
            if attr["Name"] == "custom:client_id":
                return attr["Value"]

        logger.warning("[extract_client_id_from_connected_user]: client_id not found")
        return None

    except Exception as ex:
        logger.error("[extract_client_id_from_connected_user]: %s", repr(ex))
        return None


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


@app.put("/client-manager/user-attributes")
def update_user_attributes_with_client_id():
    """
    Inserts 'client_id' inside Cognito User Attributes
    """
    logger.info("/client-manager/user-attributes route invoked")
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
        return {"message": "Optional attributes updated successfully"}, 204

    except Exception as e:
        logger.error("Error updating client id: %s", repr(e))
        return {"message": "Internal server error"}, 500


@app.get("/client-manager/client-additional/<user_id>")
def get_optional_attributes(user_id: str):
    """
    Retrieves optional fields for a client from the ClientRegistrations table
    """
    logger.info("/client-manager/client-additional GET route invoked")
    try:

        # Extract the client_id from the cognito user attributes
        client_id = extract_client_id_from_connected_user(user_id)

        # Retrieve the item from DynamoDB
        response = dynamodb_client.get_item(
            TableName=os.getenv("CLIENT_REGISTRATIONS_TABLE_NAME"),
            Key={"clientId": {"S": client_id}},
        )
        logger.debug("[get_optional_attributes]: %s", response)

        if response.get("ResponseMetadata", {}).get("HTTPStatusCode") != 200:
            logger.error("[get_optional_attributes]: %s", response)
            return {"message": "Failed to retrieve optional attributes"}, 500

        item = response.get("Item")
        if not item:
            return {"message": "client_id not found"}, 404

        # Extract optional fields
        a11y_uri = item.get("a11yUri", {}).get("S")
        back_button_enabled = item.get("backButtonEnabled", {}).get("BOOL")
        localized_content_map = item.get("localizedContentMap", {}).get("M")

        # Convert localized_content_map from DynamoDB format to JSON
        localized_content = None
        if localized_content_map:
            localized_content = LocalizedContentMap.from_dynamodb(localized_content_map)

        return {
            "a11yUri": a11y_uri,
            "backButtonEnabled": back_button_enabled,
            "localizedContentMap": localized_content,
        }, 200

    except Exception as e:
        logger.error("Error retrieving optional attributes: %s", repr(e))
        return {"message": "Internal server error"}, 500


@app.put("/client-manager/client-additional/<user_id>")
def create_or_update_optional_attributes(user_id: str):
    """
    Updates optional fields on 'ClientRegistrations' Table
    """
    logger.info("/client-manager/client-additional route invoked")
    try:
        # Parse the JSON body of the request
        body = app.current_event.json_body

        # Validate the body (optional)
        if not body:
            return {"message": "Request body is required"}, 400

        # Extract the client_id from the cognito user attributes
        client_id = extract_client_id_from_connected_user(user_id)

        # Check if client_id exists in ClientRegistrations table
        if not check_client_id_exists(client_id):
            return {"message": "client_id not found"}, 404
        # Extract optional attributes from the request body
        a11y_uri = body.get("a11yUri")
        back_button_enabled = body.get("backButtonEnabled")
        localized_content = body.get("localizedContentMap")

        localized_content_map_object = LocalizedContentMap.from_json(localized_content)
        logger.debug("[create_or_update_optional_attributes]: %s", localized_content_map_object)
        localized_content_map_object_value = localized_content_map_object.to_dynamodb()
        logger.debug(
            "[create_or_update_optional_attributes]: %s", localized_content_map_object_value
        )
        # Update the optional attributes in DynamoDB in ClientRegistrations table
        response = dynamodb_client.update_item(
            TableName=os.getenv("CLIENT_REGISTRATIONS_TABLE_NAME"),
            Key={"clientId": {"S": client_id}},
            UpdateExpression="SET a11yUri = :a11yUri, backButtonEnabled = :backButtonEnabled, localizedContentMap = :localizedContentMap",
            ExpressionAttributeValues={
                ":a11yUri": {"S": a11y_uri},
                ":backButtonEnabled": {"BOOL": back_button_enabled},
                ":localizedContentMap": localized_content_map_object_value,
            },
        )
        logger.debug("[create_or_update_optional_attributes]: %s", response)

        # Check if the response indicates success
        if response.get("ResponseMetadata", {}).get("HTTPStatusCode") != 200:
            logger.error("[create_or_update_optional_attributes]: %s", response)
            return {"message": "Failed to update optional attributes"}, 500

        # Return success response
        return {"message": "Optional attributes updated successfully"}, 204

    except Exception as e:
        logger.error("Error updating optional attributes: %s", repr(e))
        return {"message": "Internal server error"}, 500


#region Internal IDP related routes
@app.post("/client-manager/client-users")
def create_idp_internal_user():
    """
    Creates a user in the Internal IDP
    """
    logger.info("/client-manager/client-users POST route invoked")
    try:
        # Parse the JSON body of the request
        body = app.current_event.json_body

        # Validate the body (optional)
        if not body:
            logger.error("[create_idp_internal_user]: Request body is required")
            return {"message": "Request body is required"}, 400

        user_id = body.get("user_id")

        if not user_id:
            logger.error("[create_idp_internal_user]: user_id is required")
            return {"message": "user_id is required"}, 400

        # Extract the client_id from the cognito user attributes
        client_id = extract_client_id_from_connected_user(user_id)

        if not client_id:
            logger.error("[create_idp_internal_user]: client_id not found in user attributes")
            return {"message": "client_id not found in user attributes"}, 400

        # Extract the username from the request body
        username = body.get("username")

        # Extract the password from the request body
        password = body.get("password")

        # Extract the samlAttributes from the request body
        saml_attributes = body.get("samlAttributes", {})

        if not username or not password or not saml_attributes:
            logger.error("[create_idp_internal_user]: Missing required fields")
            # Return an error response indicating the missing fields
            return {"message": "username, password and saml_attributes are required"}, 400

        # Ensure that samlAttributes only contains valid keys

        invalid_keys = set(saml_attributes) - valid_saml_attributes
        if invalid_keys:
            logger.error("[create_idp_internal_user]: Invalid saml attributes: %s", invalid_keys)
            # Return an error response with the invalid keys and valid attributes
            return {
            "message": f"Invalid saml attribute(s): {', '.join(invalid_keys)}. Valid attributes are: {', '.join(valid_saml_attributes)}"
            }, 400

        # Create user in Internal IDP
        response = dynamodb_client.put_item(
            TableName=os.getenv("IDP_INTERNAL_USERS_TABLE_NAME"),
            Item={
                "username": {"S": username},
                "namespace": {"S": client_id},
                "password": {"S": password},
                "samlAttributes": {
                    "M": {
                        k: {"N": str(v)} if k == "spidLevel" else {"S": str(v)}
                        for k, v in saml_attributes.items()
                    }
                },
            },
        )
        logger.debug("[create_idp_internal_user]: %s", response)

        # Check if the response indicates success
        if response.get("ResponseMetadata", {}).get("HTTPStatusCode") != 200:
            logger.error("[create_idp_internal_user]: %s", response)
            return {"message": "Failed to create user"}, 500

        # Return success response
        return {"message": "User created successfully"}, 201

    except Exception as e:
        logger.error("Error creating user: %s", repr(e))
        return {"message": "Internal server error"}, 500


@app.put("/client-manager/client-users/<user_id>/<username>")
def update_idp_internal_user(user_id: str, username: str):
    """
    Updates a user in the Internal IDP
    """
    logger.info("/client-manager/client-users PUT route invoked")
    try:
        # Parse the JSON body of the request
        body = app.current_event.json_body

        # Validate the body (optional)
        if not body:
            logger.error("[update_idp_internal_user]: Request body is required")
            return {"message": "Request body is required"}, 400

        # Extract the client_id from the cognito user attributes
        client_id = extract_client_id_from_connected_user(user_id)

        if not client_id:
            logger.error("[update_idp_internal_user]: client_id not found in user attributes")
            return {"message": "client_id not found in user attributes"}, 400

        # Extract the samlAttributes from the request body
        saml_attributes = body.get("samlAttributes", {})

        if not saml_attributes:
            logger.error("[update_idp_internal_user]: samlAttributes are required")
            return {"message": "samlAttributes are required"}, 400

        # Ensure that samlAttributes only contains valid keys
        invalid_keys = set(saml_attributes) - valid_saml_attributes
        if invalid_keys:
            logger.error("[update_idp_internal_user]: Invalid saml attributes: %s", invalid_keys)
            return {
                "message": f"Invalid saml attribute(s): {', '.join(invalid_keys)}. Valid attributes are: {', '.join(valid_saml_attributes)}"
            }, 400

        # Update user in Internal IDP
        response = dynamodb_client.update_item(
            TableName=os.getenv("IDP_INTERNAL_USERS_TABLE_NAME"),
            Key={
                "username": {"S": username},
                "namespace": {"S": client_id},
            },
            UpdateExpression="SET samlAttributes = :samlAttributes",
            ExpressionAttributeValues={
                ":samlAttributes": {
                    "M": {
                        k: {"N": str(v)} if k == "spidLevel" else {"S": str(v)}
                        for k, v in saml_attributes.items()
                    }
                }
            },
        )
        logger.debug("[update_idp_internal_user]: %s", response)

        # Check if the response indicates success
        if response.get("ResponseMetadata", {}).get("HTTPStatusCode") != 200:
            logger.error("[update_idp_internal_user]: %s", response)
            return {"message": "Failed to update user"}, 500

        # Return success response
        return {"message": "User update successfully"}, 200

    except Exception as e:
        logger.error("Error updating user: %s", repr(e))
        return {"message": "Internal server error"}, 500


@app.delete("/client-manager/client-users/<user_id>/<username>")
def delete_idp_internal_user(user_id: str, username: str):
    """
    Deletes a user in the Internal IDP
    """
    logger.info("/client-manager/client-users DELETE route invoked")
    try:
        # Extract the client_id from the cognito user attributes
        client_id = extract_client_id_from_connected_user(user_id)

        if not client_id:
            logger.error("[delete_idp_internal_user]: client_id not found in user attributes")
            return {"message": "client_id not found in user attributes"}, 400

        # Delete user in Internal IDP
        response = dynamodb_client.delete_item(
            TableName=os.getenv("IDP_INTERNAL_USERS_TABLE_NAME"),
            Key={
                "username": {"S": username},
                "namespace": {"S": client_id},
            },
        )
        logger.debug("[delete_idp_internal_user]: %s", response)

        # Check if the response indicates success
        if response.get("ResponseMetadata", {}).get("HTTPStatusCode") != 200:
            logger.error("[delete_idp_internal_user]: %s", response)
            return {"message": "Failed to delete user"}, 500

        # Return success response
        return {"message": "User deleted successfully"}, 204

    except Exception as e:
        logger.error("Error deleting user: %s", repr(e))
        return {"message": "Internal server error"}, 500


@app.get("/client-manager/client-users/<user_id>")
def get_idp_internal_users(user_id: str):
    """
    Retrieves all users of a client in the Internal IDP
    """
    logger.info("/client-manager/client-users GET route invoked")
    try:
        # Extract the client_id from the cognito user attributes
        client_id = extract_client_id_from_connected_user(user_id)

        if not client_id:
            logger.error("[get_idp_internal_users]: client_id not found in user attributes")
            return {"message": "client_id not found in user attributes"}, 400

        # Retrieve all users in Internal IDP using a GSI on 'namespace'
        users = []
        last_evaluated_key = None
        while True:
            query_kwargs = {
                "TableName": os.getenv("IDP_INTERNAL_USERS_TABLE_NAME"),
                "IndexName": os.getenv("IDP_INTERNAL_USERS_GSI_NAME"),
                "KeyConditionExpression": "namespace = :namespace",
                "ExpressionAttributeValues": {":namespace": {"S": client_id}},
            }
            if last_evaluated_key:
                query_kwargs["ExclusiveStartKey"] = last_evaluated_key

            response = dynamodb_client.query(**query_kwargs)

            if response.get("ResponseMetadata", {}).get("HTTPStatusCode") != 200:
                logger.error("[get_idp_internal_users]: %s", response)
                return {"message": "Failed to retrieve users"}, 500

            items = response.get("Items", [])
            users.extend([
                {
                    "username": user["username"]["S"],
                    "password": user["password"]["S"],
                    "samlAttributes": {
                        k: v["S"] if "S" in v else v["N"]
                        for k, v in user.get("samlAttributes", {}).get("M", {}).items()
                    },
                }
                for user in items
            ])

            last_evaluated_key = response.get("LastEvaluatedKey")
            if not last_evaluated_key:
                break

        return {"users": users}, 200

    except Exception as e:
        logger.error("Error retrieving users: %s", repr(e))
        return {"message": "Internal server error"}, 500
#endregion

@tracer.capture_lambda_handler
def handler(event: dict, context: LambdaContext) -> dict:
    """
    Lambda handler that call routes of aws_lambda_powertools
    """
    return app.resolve(event, context)
