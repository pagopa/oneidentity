# One Identity

## Technology Stack 📚

- Java 21 Runtime Environment GraalVM CE
- [Quarkus](https://quarkus.io/)

## Start Project Locally 🚀

### Prerequisites

- git
- maven (v3.9.6)
- jdk-21
- docker

## Develop Locally 💻
Be sure to start from right directory
```shell
cd src/oneid
```

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell
./mvnw quarkus:dev -P $PROFILE
```
This is the full list of available profiles:
- oneid-ecs-core-aggregate
- oneid-lambda-client-registration-aggregate
- oneid-lambda-service-metadata-aggregate
- oneid-lambda-update-idp-metadata-aggregate
- oneid-lambda-is-gh-integration-aggregate
- oneid-all

### Docker usage
We can choose different approach to use docker. Each service has its own Dockerfile and it can run by issuing following command:

`docker build -f oneid-ecs-core/Dockerfile -t local/oneid-ecs-core .`

Then

`docker run -i --rm -p 8080:8080 local/oneid-ecs-core`

*This will specifically run both backend and frontend*

In the other hand it is available a more handy mode which will starts all needed services to have a full One Identity instance using `docker-compose`. More details in next chapter.

## Start composing
```shell
docker compose up
```

Some are having problems running compose as plugin, we'd recommend, in those cases, to switch on `docker-compose` classic syntax, as follow:

```shell
docker-compose up
```

#### Data
In `src/oneid/docker_mock/` folder are present some useful data to configure and use One Identity correctly. Of course **all certificates and secret are for demo purpose**.

**DynamoDB commands**

Firstly we should take care about DynamoDB and its data

#### Start dynamo using docker compose
```shell
docker-compose up -d dynamodb-local
```

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
--attribute-definitions AttributeName=samlRequestID,AttributeType=S AttributeName=recordType,AttributeType=S AttributeName=code,AttributeType=S  \
--key-schema AttributeName=samlRequestID,KeyType=HASH AttributeName=recordType,KeyType=RANGE \
--global-secondary-indexes file:///home/dynamodblocal/gsi_code.json
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
--global-secondary-indexes file://gsi_pointer.json \
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


