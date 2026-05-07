# oneid-lambda-service-metadata

## Purpose

`oneid-lambda-service-metadata` is the Quarkus module responsible for service metadata generation and related storage integrations.

Based on its current runtime configuration, it integrates with:

- DynamoDB
- S3
- SSM

Unlike `oneid-lambda-client-registration`, this module is not part of the default `src/oneid/docker-compose.yaml` local stack.

## Local Development

Run all commands from the Maven workspace root:

```shell
cd src/oneid
```

### Prerequisites

- JDK 21
- Docker
- Docker Compose

### Run in Quarkus dev mode

The module has an aggregate profile in the workspace root:

```shell
cd src/oneid
./mvnw quarkus:dev -P oneid-lambda-service-metadata-aggregate -DskipDepcheck=true
```

Current `skipDepcheck` behavior is:

- default: depcheck enabled
- `-DskipDepcheck=false`: depcheck enabled
- `-DskipDepcheck=true`: depcheck disabled

If you want depcheck enabled, provide your Maven settings file:

```shell
cd src/oneid
./mvnw quarkus:dev -P oneid-lambda-service-metadata-aggregate -s settings.xml
```

### Local AWS-compatible services

If you want to run against the same MiniStack used by the repository local stack, start it first:

```shell
cd src/oneid
docker compose up oneid-aws-local oneid-services-seeder
```

The current dev profile already points S3 to `http://localhost:4566`, but DynamoDB defaults to `http://localhost:8000`.

When using the compose-provided MiniStack from the host, override DynamoDB explicitly:

```shell
cd src/oneid
./mvnw quarkus:dev \
	-P oneid-lambda-service-metadata-aggregate \
	-DskipDepcheck=true \
	-Dquarkus.dynamodb.endpoint-override=http://localhost:4566
```

Depending on the code path you are testing, you may also need to provide runtime values such as:

- `SERVICE_METADATA_BUCKET_NAME`
- `BASE_PATH`
- `ENTITY_ID`
- `ACS_URL`
- `SLO_URL`

### Docker image build

Fast local build without depcheck:

```shell
cd src/oneid
docker build -f oneid-lambda-service-metadata/Dockerfile --build-arg SKIP_DEPCHECK=true -t local/oneid-lambda-service-metadata .
```

Depcheck-enabled build with BuildKit secret:

```shell
cd src/oneid
DOCKER_BUILDKIT=1 docker build \
	--secret id=maven_settings,src=settings.xml \
	-f oneid-lambda-service-metadata/Dockerfile \
	-t local/oneid-lambda-service-metadata .
```

### Useful Commands

Follow logs when the module is running in Docker:

```shell
cd src/oneid
docker logs -f oneid-lambda-service-metadata
```

Inspect local AWS-compatible resources from the host:

```shell
export AWS_ACCESS_KEY_ID=test-key
export AWS_SECRET_ACCESS_KEY=test-secret
export AWS_REGION=eu-south-1
aws s3 ls --endpoint-url http://localhost:4566
```

## Notes

- This module is built from the workspace root `src/oneid/pom.xml` through the `oneid-lambda-service-metadata-aggregate` profile.
- The root repository README contains the full local stack documentation and seeder behavior.
