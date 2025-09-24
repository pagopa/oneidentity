"""
Lambda function to process SQS messages and send PATCH requests to an external API.
"""
import os
import json
import boto3
import urllib3

http = urllib3.PoolManager()

ssm = boto3.client('ssm')

ENDPOINT_URL = os.environ['PDV_BASE_URL']

def get_api_key(client_id):
    """Retrieves the API key from AWS SSM Parameter Store."""
    param_name = f"/pdv/{client_id}"
    response = ssm.get_parameter(Name=param_name, WithDecryption=True)
    return response['Parameter']['Value']

def handler(event, context):
    """
    Processes records from an SQS event, constructs, and sends a PATCH request.
    """
    for record in event['Records']:
        body = json.loads(record['body'])
        client_id = body['clientId']
        save_pdv_user_dto = body['savePDVUserDTO']

        api_key = get_api_key(client_id)

        headers = {
            'x-api-key': api_key,
            'Content-Type': 'application/json'
        }

        encoded_body = json.dumps(save_pdv_user_dto).encode('utf-8')

        try:
            response = http.request(
                'PATCH',
                f"{ENDPOINT_URL}/user-registry/v1/users",
                headers=headers,
                body=encoded_body,
                timeout=10.0
            )

            if response.status >= 300:
                error_text = response.data.decode('utf-8')
                raise Exception(f"Request failed: {response.status} - {error_text}")

        except urllib3.exceptions.MaxRetryError as e:
            raise Exception(f"Connection error: {e}") from e

    return {'status': 'done'}