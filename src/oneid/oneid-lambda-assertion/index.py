import boto3
import logging
import json

logger = logging.getLogger()
logger.setLevel(logging.INFO)

s3 = boto3.client('s3')

def lambda_handler(event, context):

    logger.info('Event %s', event)

    data = {
        'message': 'Hello from Lambda!',
        'timestamp': str(context.get_remaining_time_in_millis())
    }
    
    # Convert data to JSON string
    file_content = json.dumps(data)
    
    # Write the file to S3
    file_key = 'test'
    bucket_name = 'assertions-6284'
    try:
        
      s3 = boto3.resource("s3")
      s3.Bucket(bucket_name).put_object(Key=file_key, Body=file_content)
        
      return {
          'statusCode': 200,
          'body': json.dumps(f'Successfully wrote file {file_key} to bucket {bucket_name}')
      }
    except Exception as e:
        print(e)
        return {
            'statusCode': 500,
            'body': json.dumps(f'Error writing file to S3: {str(e)}')
        }

    return {
        'statusCode' : 200
    }
