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

#### Configuration and mock data
In `src/oneid/docker_mock/` folder are present some useful data to configure and use One Identity correctly. Of course **all certificates and secret are for demo purpose**.
Firstly copy `.env.example` in `.env` and fill the required information.

In the other hand it is available a more handy mode which will starts all needed services to have a full One Identity instance using `docker-compose`. More details in next chapter.

## Start composing
```shell
docker compose up
```

Some are having problems running compose as plugin, we'd recommend, in those cases, to switch on `docker-compose` classic syntax, as follow:

```shell
docker-compose up
```

Extra note: `docker-compose` sometimes can be annoyng after a single restart saying it is not capabale to rebuild container, just `docker-compose down` before restart and everything should be fine.

## Surfing OI
Now, you are able to login using an IDP test preconfigured.
1. just navigate using your browser to http://localhost:8084
2. click on "Entra con SPID/CIE"
3. Click on Entra con SPID
4. Select the IDP
5. Login with validator/validator
6. On the left click on Metadata SP -> Download from URL and fill with "http://172.17.0.1:8082/saml/spid/metadata" and then click on Download button.
7. Click on Response -> Check Response
8. On the right click on "Invia response al SP"
9. Et voilà.

## Developing oneid-core
Docker will make things easy for us as we can have a local dynamodb instance with necessary resources needed for developing.
If we don't want to compose the whole One Identity we can still focus our attention on frontend and backend stuff by doing:

```shell
cd src/oneid
docker-compose up dynamodb-local dynamo-import
```
This will start our dynamo complete instance.

```shell
cd src/oneid
./mvnw quarkus:dev -P oneid-ecs-core-aggregate
```
This will start our backend and frontend instance thanks to Quarkus plugin Quinoa, just navigate to http://localhost:8080

### Interact with OI components
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

**SSM debug commands**
Some useful debugging commands for SSM, first let's get into runnung docker instance
```shell
docker exec -it aws-local-ssm bash
```

Then try to retrieve some values
```shell
export AWS_ACCESS_KEY_ID=test-key
export AWS_SECRET_ACCESS_KEY=test-secret
aws ssm get-parameter --region eu-south-1 --endpoint http://localhost:4566 --name "cert.pem"
```

# Code of Conduct
For a comprehensive Code of Conduct please refer to the [PagoPA's one](https://github.com/pagopa/ospo-utils/blob/main/common/CODE_OF_CONDUCT.md). 


# Licensing
This project is licensed under the terms of the [Mozilla Public License Version 2.0](LICENSE).