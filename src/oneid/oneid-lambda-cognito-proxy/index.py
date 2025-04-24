import boto3
import json
import requests
from jose import jwt
import logging
import os

USER_POOL_ID = os.getenv("AWS_COGNITO_UP_ID")
CLIENT_ID = os.getenv("AWS_COGNITO_CLIENT_ID")
REGION = os.getenv("AWS_REGION")
ISSUER = f"https://cognito-idp.{REGION}.amazonaws.com/{USER_POOL_ID}"
JWKS_URL = f"{ISSUER}/.well-known/jwks.json"

# Get logger instance
logger = logging.getLogger()
logger.setLevel(os.getenv("LOG_LEVEL", "INFO"))

# Cache the JWKS for performance
JWKS = requests.get(JWKS_URL).json()
COGNITO_CLIENT = boto3.client("cognito-idp", region_name=REGION)


def get_public_key(token):
    headers = jwt.get_unverified_header(token)
    kid = headers["kid"]
    for key in JWKS["keys"]:
        if key["kid"] == kid:
            return key
    raise Exception("Public key not found")


def verify_token(token):
    key = get_public_key(token)
    return jwt.decode(
        token, key, algorithms=["RS256"], audience=CLIENT_ID, issuer=ISSUER
    )


def lambda_handler(event, context):
    try:
        auth_header = event["headers"].get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return {
                "statusCode": 401,
                "body": json.dumps({"error": "Missing or invalid token"}),
            }

        token = auth_header.split(" ")[1]
        decoded = verify_token(token)
        user_sub = decoded["sub"]

        # Parse request body
        body = json.loads(event["body"])
        attribute = body.get("attribute")
        value = body.get("value")

        if not attribute or not value:
            return {
                "statusCode": 400,
                "body": json.dumps({"error": "Missing attribute or value"}),
            }

        # Update the user attribute
        COGNITO_CLIENT.admin_update_user_attributes(
            UserPoolId=USER_POOL_ID,
            Username=user_sub,
            UserAttributes=[{"Name": f"custom:{attribute}", "Value": value}],
        )

        return {
            "headers": {"Content-Type": "application/json", "Access-Control-Allow-Origin": "*"},
            "statusCode": 200,
            "body": json.dumps({"message": "Attribute updated successfully."}),
        }

    except Exception as e:
        return {"statusCode": 500, "body": json.dumps({"error": str(e)})}
