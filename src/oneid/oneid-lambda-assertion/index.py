import boto3
import os
import logging
import json
from datetime import datetime, timedelta, timezone
import dateutil.tz


logger = logging.getLogger()
logger.setLevel(logging.INFO)

s3 = boto3.resource("s3")
bucket_name = os.environ['S3_BUCKET']

def convert_to_cet(creation_time):
    # Convert epoch time to UTC
    timezone = dateutil.tz.gettz('Europe/Rome')
    # Convert UTC to Central European Time (CET/CEST)
    cet_time = datetime.fromtimestamp(creation_time, tz=timezone)
    return cet_time

def lambda_handler(event, context):

    for record in event:
        
        saml_request_id = record['dynamodb']['NewImage']['samlRequestID']['S']
        record_type = record['dynamodb']['NewImage']['recordType']['S']
        creation_time = record['dynamodb']['NewImage']['creationTime']['N']
        contentBody = record['dynamodb']['NewImage']
        cet_time = convert_to_cet(int(creation_time))
       
       
    # Convert data to JSON string
    file_content = json.dumps(contentBody)
    
    # Write the file to S3
    file_key = cet_time.strftime(f"%Y/%m/%d/%H/%M/{record_type}/{saml_request_id}.json")
    try:
        
      s3.Bucket(bucket_name).put_object(Key=file_key, Body=file_content)
        
      return {
          'statusCode': 200,
          'body': json.dumps(f'Successfully wrote file {file_key} to bucket {bucket_name}')
      }
    except Exception as e:
        logger.error(e)
        return {
            'statusCode': 500,
            'body': json.dumps(f'Error writing file to S3: {str(e)}')
        }

    return {
        'statusCode' : 200
    }