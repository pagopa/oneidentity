# Docker usage
Firstly we should take care about DynamoDB and its data

## Start dynamo using docker compose
```shell
cd src/oneid
docker-compose up -d dynamodb-local
```

## DynamoDB local
### Check tables
```shell
aws dynamodb list-tables --endpoint-url http://localhost:8000 --region eu-south-1 --profile local
```

### Create tables
```shell
aws dynamodb create-table \
--table-name ClientRegistrations \
--attribute-definitions AttributeName=clientId,AttributeType=S \
--key-schema AttributeName=clientId,KeyType=HASH \
--provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
--endpoint-url http://localhost:8000 \
--region eu-south-1 \
--profile local
```

```shell
aws dynamodb create-table \
--table-name Sessions \
--attribute-definitions AttributeName=samlRequestID,AttributeType=S AttributeName=recordType,AttributeType=S \
--key-schema AttributeName=samlRequestID,KeyType=HASH AttributeName=recordType,KeyType=RANGE \
--provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
--endpoint-url http://localhost:8000 \
--region eu-south-1 \
--profile local
```

Before create IDPMetadata table make sure you have the global index file definition, that should looks like `gsi.json`:
```json
[
  {
    "IndexName": "gsi_pointer_idx",
    "KeySchema": [
      {
        "AttributeName": "pointer",
        "KeyType": "HASH"
      }
    ],
    "Projection": {
      "ProjectionType": "ALL"
    },
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 10,
      "WriteCapacityUnits": 5
    }
  }
]
```

```shell
aws dynamodb create-table \
--table-name IDPMetadata \
--attribute-definitions AttributeName=entityID,AttributeType=S AttributeName=pointer,AttributeType=S \
--key-schema AttributeName=entityID,KeyType=HASH AttributeName=pointer,KeyType=RANGE \
--global-secondary-indexes file://gsi.json \
--provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
--endpoint-url http://localhost:8000 \
--region eu-south-1 \
--profile local
```

### Insert data
```shell
aws dynamodb put-item --table-name IDPMetadata --item file://idps.json
```

#### Batch export from dev
Make sure to have right credentials
```shell
export AWS_PROFILE=DevOI
aws dynamodb scan --table-name IDPMetadata --max-items 20 --output json > ./export.json
```

#### Convert to BatchWrite syntax
```shell
cat export.json | jq "{\"IDPMetadata\": [.Items[] | {PutRequest: {Item: .}}]}" > BatchWriteItem.txt
```

#### Batch import into local
```shell
export AWS_PROFILE=local
aws dynamodb batch-write-item --request-items file://BatchWriteItem.txt --endpoint-url http://localhost:8000 --region eu-south-1 --profile local
```

## Start composing
```shell
docker compose up
```
