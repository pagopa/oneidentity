import boto3
import os
import logging
import json
from datetime import datetime, timedelta, timezone
import dateutil.tz
import base64


logger = logging.getLogger()
logger.setLevel(logging.INFO)

s3 = boto3.resource("s3")
bucket_name = os.environ['S3_BUCKET']

def decode_base64_content(content):
    
    return base64.b64decode(content).decode('utf-8')
    
def convert_to_cet(creation_time):
    # Convert epoch time to UTC
    timezone = dateutil.tz.gettz('Europe/Rome')
    # Convert UTC to Central European Time (CET/CEST)
    return datetime.fromtimestamp(creation_time, tz=timezone)
    

def lambda_handler(event, context):

    try:
    
        for record in event:
        
            saml_request_id = "".join([record['samlRequestID'][0].replace("_", "", 1), record['samlRequestID'][1:]])
            record_type = record['recordType']
            creation_time = record['creationTime']
            cet_time = convert_to_cet(int(creation_time))
        
            if record_type == "SAML" :
                record['SAMLRequest'] = decode_base64_content(record['SAMLRequest'])
                record['SAMLResponse'] = decode_base64_content(record['SAMLResponse'])
            
            # Write the file to S3
            file_key = cet_time.strftime(f"%Y/%m/%d/%H/%M/{record_type}/{saml_request_id}.json")
                
            s3.Bucket(bucket_name).put_object(Key=file_key, Body=json.dumps(record))
        
    except Exception as e:
        logger.error(e)
        return {
            'statusCode': 500,
            'body': json.dumps(f'Error writing file to S3: {str(e)}')
        }

    return {
        'statusCode' : 200
    }