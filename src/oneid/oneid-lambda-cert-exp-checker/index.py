import boto3
import os
from cryptography import x509
from cryptography.hazmat.backends import default_backend
import datetime

param_name = os.environ['PARAM_NAME']
sns_topic  = os.environ['SNS_TOPIC']

def lambda_handler(event, context):
    ssm = boto3.client('ssm')
    sns = boto3.client('sns')

    # Get certificate from Parameter Store
    param = ssm.get_parameter(Name= param_name, WithDecryption=True)
    cert_pem = param['Parameter']['Value'].encode()

    # Parse certificate
    cert = x509.load_pem_x509_certificate(cert_pem, default_backend())
    expiry_date = cert.not_valid_after
    days_left = (expiry_date - datetime.datetime.utcnow()).days

    if days_left <= 7:
        message = f"⚠️ Certificate '{param['Parameter']['Name']}' expires in {days_left} days on {expiry_date}."
        sns.publish(
            TopicArn='${sns_topic}',
            Subject='Certificate Expiry Warning',
            Message=message
        )
        print("Notification sent.")
    else:
        print(f"Certificate is valid for {days_left} more days.")
