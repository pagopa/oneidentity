"""Lambda function which serves as BE for Client Portal"""

import logging
import os
import json
import base64
import jwt
from typing import Optional

import boto3
from aws_lambda_powertools import Tracer
from aws_lambda_powertools.event_handler import APIGatewayRestResolver
from aws_lambda_powertools.utilities.typing import LambdaContext

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


def get_user_id_from_bearer(bearer: str) -> Optional[str]:
    """
    Validates a bearer token string, parses it, and extracts the 'sub' (subject) claim.
    """
    if not bearer or not bearer.startswith("Bearer "):
        return None

    token = bearer[len("Bearer ") :]
    try:
        payload = jwt.decode(token, options={"verify_signature": False})
        user_id = payload.get("sub")
    except Exception as ex:
        return None
    if not user_id:
        return None
    return user_id


def extract_client_id_from_connected_user(user_id: str) -> Optional[str]:
    """
    Extracts the client_id from the connected user using the user_id.
    """
    try:
        # Get the user attributes from Cognito
        response = dynamodb_client.scan(
            TableName=os.getenv("CLIENT_REGISTRATIONS_TABLE_NAME"),
            FilterExpression="userId = :userId",
            ExpressionAttributeValues={":userId": {"S": user_id}}
        )
        items = response.get("Items", [])
        if items:
            return items[0]["clientId"]["S"]
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


@app.post("/client-manager/client-users")
def create_idp_internal_user():
    """
    Creates a user in the Internal IDP
    """
    logger.info("/client-manager/client-users POST route invoked")
    try:
        # Extract the user_id from the bearer token
        bearer = app.current_event.headers.get("Authorization", "")
        user_id = get_user_id_from_bearer(bearer)

        if not user_id:
            logger.error("[create_idp_internal_user]: user_id is required")
            return {"message": "user_id is required"}, 400
        
        # Parse the JSON body of the request
        body = app.current_event.json_body

        # Validate the body (optional)
        if not body:
            logger.error("[create_idp_internal_user]: Request body is required")
            return {"message": "Request body is required"}, 400

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
            ConditionExpression="attribute_not_exists(username) AND attribute_not_exists(namespace)",
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


    except dynamodb_client.exceptions.ConditionalCheckFailedException:
        logger.error("[create_idp_internal_user]: User already exists")
        return {"message": "User already exists"}, 409
    except Exception as e:
        logger.error("Error creating user: %s", repr(e))
        return {"message": "Internal server error"}, 500


@app.patch("/client-manager/client-users/<username>")
def update_idp_internal_user(username: str):
    """
    Updates a user in the Internal IDP
    """
    logger.info("/client-manager/client-users PATCH route invoked")
    try:
        # Extract the user_id from the bearer token
        bearer = app.current_event.headers.get("Authorization", "")
        user_id = get_user_id_from_bearer(bearer)
        if not user_id:
            logger.error("[update_idp_internal_user]: user_id is required")
            return {"message": "user_id is required"}, 400

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
            ConditionExpression="attribute_exists(username) AND attribute_exists(namespace)",
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
    
    except dynamodb_client.exceptions.ConditionalCheckFailedException:
        logger.error("[update_idp_internal_user]: User does not exist")
        return {"message": "User does not exist"}, 404

    except Exception as e:
        logger.error("Error updating user: %s", repr(e))
        return {"message": "Internal server error"}, 500


@app.delete("/client-manager/client-users/<username>")
def delete_idp_internal_user(username: str):
    """
    Deletes a user in the Internal IDP
    """
    logger.info("/client-manager/client-users DELETE route invoked")
    try:

        # Extract the user_id from the bearer token
        bearer = app.current_event.headers.get("Authorization", "")
        user_id = get_user_id_from_bearer(bearer)
        if not user_id:
            logger.error("[delete_idp_internal_user]: user_id is required")
            return {"message": "user_id is required"}, 400

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


@app.get("/client-manager/client-users")
def get_idp_internal_users():
    """
    Retrieves all users of a client in the Internal IDP
    """
    logger.info("/client-manager/client-users GET route invoked")
    try:
        # Extract the user_id from the bearer token
        bearer = app.current_event.headers.get("Authorization", "")
        user_id = get_user_id_from_bearer(bearer)
        if not user_id:
            logger.error("[get_idp_internal_users]: user_id is required")
            return {"message": "user_id is required"}, 400
        
        # Extract the client_id from the cognito user attributes
        client_id = extract_client_id_from_connected_user(user_id)

        if not client_id:
            logger.error("[get_idp_internal_users]: client_id not found in user attributes")
            return {"message": "client_id not found in user attributes"}, 400
        
        query_params = app.current_event.query_string_parameters or {}
        limit = query_params.get("limit", 10)
        try:
            limit = int(limit)
            # TODO Reset limit to 50 after adding fe-side pagination and input search parameters
            if limit <= 0 or limit > 1000:
                raise ValueError("Limit must be between 1 and 1000")
        except ValueError:
            logger.error("[get_idp_internal_users]: Invalid limit value")
            return {"message": "Invalid limit value"}, 400
                

        # Retrieve all users in Internal IDP using a GSI on 'namespace'
        users = []
        last_evaluated_key = query_params.get("last_evaluated_key", None)

        if last_evaluated_key:
            try:
                json_bytes = base64.b64decode(last_evaluated_key.encode('utf-8'))
                last_evaluated_key = json.loads(json_bytes.decode('utf-8'))
            except (TypeError, ValueError) as e:
                logger.error("[get_idp_internal_users]: Invalid last_evaluated_key: %s", repr(e))
                return {"message": "Invalid last_evaluated_key"}, 400

        query_kwargs = {
            "TableName": os.getenv("IDP_INTERNAL_USERS_TABLE_NAME"),
            "IndexName": os.getenv("IDP_INTERNAL_USERS_GSI_NAME"),
            "KeyConditionExpression": "namespace = :namespace",
            "ExpressionAttributeValues": {":namespace": {"S": client_id}},
        }
        if limit:
            query_kwargs["Limit"] = limit

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

        last_evaluated_key = response.get("LastEvaluatedKey", None)

        if last_evaluated_key:
            # Base 64 encode the last evaluated key for the response
            json_string = json.dumps(last_evaluated_key, separators=(',', ':'))
            
            base64_bytes = base64.b64encode(json_string.encode('utf-8'))

            last_evaluated_key = base64_bytes.decode('utf-8')

        return {"users": users, "last_evaluated_key": last_evaluated_key}, 200

    except Exception as e:
        logger.error("Error retrieving users: %s", repr(e))
        return {"message": "Internal server error"}, 500

@tracer.capture_lambda_handler
def handler(event: dict, context: LambdaContext) -> dict:
    """
    Lambda handler that call routes of aws_lambda_powertools
    """
    return app.resolve(event, context)
