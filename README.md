# One Identity

## Technology Stack

- Java 21
- Maven Wrapper (`src/oneid/mvnw`)
- [Quarkus](https://quarkus.io/)
- Docker and Docker Compose
- MiniStack for local AWS-compatible services (`SSM`, `DynamoDB`, `KMS`, `S3`)

## Local Development

The local development entry point for the application is the Maven workspace under `src/oneid`.

```shell
cd src/oneid
```

### Prerequisites

- `git`
- `docker`
- Docker Compose plugin (`docker compose`)
- JDK 21
- Maven 3.9.x if you do not want to rely on the wrapper
- `aws` CLI if you want to inspect local `SSM`, `DynamoDB`, or `KMS` from the host

### Local Stack Overview

The local stack defined in `src/oneid/docker-compose.yaml` starts these components:

- `oneid-aws-local`: local AWS-compatible services on `http://localhost:4566`
- `oneid-services-seeder`: initializes local AWS resources and exits successfully when seeding is complete
- `oneid-ecs-core`: Quarkus backend service
- `oneid-ecs-internal-idp`: internal IDP service
- `oneid-fe`: Vite frontend dev server
- `oneid-gateway`: nginx gateway exposed on `http://localhost:8080`
- `oneid-lambda-client-registration`: local Quarkus lambda container exposed on `http://localhost:8081`
- `oneid-dummy-client`: demo client exposed on `http://localhost:8084`

The gateway is the normal browser entry point for the application. It routes:

- `/`, `/login`, `/assets` to `oneid-fe`
- `/oidc`, `/idps`, `/clients`, `/authorize`, `/logout`, `/metadata`, `/health`, `/saml` to `oneid-ecs-core`
- `/idp/` to `oneid-ecs-internal-idp`
- `/client` to `oneid-dummy-client`

### First-Time Setup

#### 1. No manual dummy client secret setup is required

The dummy client credentials are generated at runtime by `oneid-services-seeder`.

The seeder renders a runtime env file for the dummy client from `src/oneid/docker_mock/oneid-aws-seed/dummy-client.env.template`, so you do not need to create or update a checked-in `.env` file before starting the local stack.

#### 2. Optional: configure Maven settings for depcheck-enabled builds

The repository uses the `it.pagopa.maven:depcheck` plugin from GitHub Packages.

Current `skipDepcheck` behavior is:

- default: depcheck enabled
- `-DskipDepcheck=false`: depcheck enabled
- `-DskipDepcheck=true`: depcheck disabled

For direct Maven commands or Docker builds where depcheck is enabled, provide a Maven `settings.xml` with GitHub Packages credentials:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

The token only needs `read:packages` scope for local development.

### Recommended Local Workflow

#### Full stack with Docker Compose

Run the full local stack from `src/oneid`:

```shell
cd src/oneid
docker compose up --build
```

If you only need to rebuild a single service while keeping the stack behavior consistent, run for example:

```shell
cd src/oneid
docker compose up --build oneid-ecs-internal-idp
```

If the Compose plugin is unavailable in your environment, the classic fallback is:

```shell
cd src/oneid
docker-compose up --build
```

To reset containers, networks, and named volumes created by the stack:

```shell
cd src/oneid
docker compose down -v
```

#### Backend-focused workflow

If you want local AWS resources and seed data but prefer to run Java code outside Docker:

```shell
cd src/oneid
docker compose up oneid-aws-local oneid-services-seeder
```

Then start the backend with Maven:

```shell
cd src/oneid
./mvnw quarkus:dev -P oneid-ecs-core-aggregate -DskipDepcheck=true
```

If you want depcheck enabled instead of skipped, use your Maven settings file:

```shell
cd src/oneid
./mvnw quarkus:dev -P oneid-ecs-core-aggregate -s settings.xml
```

The aggregate profiles currently available in `src/oneid/pom.xml` are:

- `oneid-ecs-core-aggregate`
- `oneid-lambda-client-registration-aggregate`
- `oneid-lambda-service-metadata-aggregate`
- `oneid-lambda-update-idp-metadata-aggregate`
- `oneid-lambda-is-gh-integration-aggregate`
- `oneid-all`

### What the Seeder Actually Does

`src/oneid/docker_mock/oneid-aws-seed/oneid-seeder.sh` is the source of truth for local resource initialization.

When `oneid-services-seeder` runs, it:

- waits for local `DynamoDB`, `SSM`, and `KMS` on `oneid-aws-local`
- generates a fresh local certificate pair for `oneid-ecs-core`
- generates a fresh local certificate pair for `oneid-ecs-internal-idp`
- generates fresh dummy client secrets and salts for the local seeded clients
- renders runtime DynamoDB seed data by injecting the generated internal IDP certificate and generated client secret material into the seed payload
- renders a runtime dummy client env file from `src/oneid/docker_mock/oneid-aws-seed/dummy-client.env.template`
- ensures a local `KMS` key exists and that alias `alias/sign-jwt` points to it
- creates DynamoDB tables if they do not already exist
- inserts demo seed data into local DynamoDB
- stores generated certificates in local `SSM`

### Validate Seeder Changes

Use these commands from `src/oneid` when you change the local seeder logic.

Run the Bash syntax check:

```shell
cd src/oneid
bash -n docker_mock/oneid-aws-seed/oneid-seeder.sh
```

Run the zero-dependency helper self-check:

```shell
cd src/oneid
python3 docker_mock/oneid-aws-seed/render_runtime_seed.py --self-check
```

Validate the helper in the same container image used by the local stack:

```shell
cd src/oneid
docker build -f docker_mock/oneid-aws-seed/Dockerfile -t local/oneid-seeder-validate .
docker run --rm --entrypoint python3 \
  -v "$PWD"/docker_mock/oneid-aws-seed:/home/aws:ro \
  -v /tmp/oneid-runtime-validate:/runtime \
  local/oneid-seeder-validate \
  /home/aws/render_runtime_seed.py \
  --dynamodb-template /home/aws/dynamodb/batchDynamo.json \
  --dummy-client-template /home/aws/dummy-client.env.template \
  --output-dynamodb /runtime/batchDynamo.runtime.validation.json \
  --output-dummy-client-env /runtime/dummy-client.validation.env \
  --certificate-base64 validation-cert
```

Validate the Compose wiring after seeder changes:

```shell
cd src/oneid
docker compose config >/tmp/oneid-docker-compose.rendered.yaml
```

The local seeder creates or refreshes these `SSM` parameters:

- `cert.pem`
- `key.pem`
- `idp_internal_cert.pem`
- `idp_internal_key.pem`

The local seeder ensures these DynamoDB tables exist:

- `ClientRegistrations`
- `Sessions`
- `IDPMetadata`
- `InternalIDPUsers`
- `InternalIDPSessions`

### Important Note About Local Certificates

The seeder generates certificates dynamically on each run. If you delete or recreate the local `SSM` parameters after services are already running, restart the services that load those credentials at startup.

In practice, after reseeding certificates, restart at least:

- `oneid-ecs-core`
- `oneid-ecs-internal-idp`

Example:

```shell
cd src/oneid
docker compose restart oneid-ecs-core oneid-ecs-internal-idp
```

### Local Ports and URLs

- `http://localhost:8080`: main local entry point through `oneid-gateway`
- `http://localhost:8081`: `oneid-lambda-client-registration`
- `http://localhost:8084`: dummy client
- `http://localhost:4566`: MiniStack endpoint for local AWS-compatible services

In the full stack:

- open `http://localhost:8084/client/login` for the dummy client directly

### Local Build Semantics for `SKIP_DEPCHECK`

Local Docker Compose intentionally sets `SKIP_DEPCHECK=true` for the services that build Java artifacts inside containers:

- `oneid-ecs-core`
- `oneid-ecs-internal-idp`
- `oneid-lambda-client-registration`

This keeps local container builds independent from GitHub Packages credentials.

When you build those images manually:

- use `SKIP_DEPCHECK=true` for a fast local-only build
- provide the `maven_settings` BuildKit secret when you want depcheck enabled

Examples:

Fast local build:

```shell
cd src/oneid
docker build -f oneid-ecs-core/Dockerfile --build-arg SKIP_DEPCHECK=true -t local/oneid-ecs-core .
```

Depcheck-enabled build:

```shell
cd src/oneid
DOCKER_BUILDKIT=1 docker build \
  --secret id=maven_settings,src=settings.xml \
  -f oneid-ecs-core/Dockerfile \
  -t local/oneid-ecs-core .
```

### Logs and Troubleshooting

Follow the main local services:

```shell
cd src/oneid
docker compose logs -f oneid-services-seeder oneid-ecs-core oneid-ecs-internal-idp oneid-gateway
```

Check container status:

```shell
cd src/oneid
docker compose ps
```

Re-run only the seeder after cleaning local data:

```shell
cd src/oneid
docker compose up oneid-services-seeder
```

### Inspect Local AWS Resources From the Host

If you use the AWS CLI against MiniStack, export any dummy credentials first:

```shell
export AWS_ACCESS_KEY_ID=test-key
export AWS_SECRET_ACCESS_KEY=test-secret
export AWS_REGION=eu-south-1
```

List local DynamoDB tables:

```shell
aws dynamodb list-tables --endpoint-url http://localhost:4566 --region eu-south-1
```

List local SSM parameters:

```shell
aws ssm describe-parameters --endpoint-url http://localhost:4566 --region eu-south-1
```

Read a local SSM parameter:

```shell
aws ssm get-parameter --name cert.pem --endpoint-url http://localhost:4566 --region eu-south-1
```

List local KMS aliases:

```shell
aws kms list-aliases --endpoint-url http://localhost:4566 --region eu-south-1
```

### Test the Local User Journey

With the full stack running:

1. Open `http://localhost:8084/client/login`.
2. Start the SPID/CIE login flow from the dummy client.
3. Follow the redirect to the local IDP pages served through the gateway.
4. Use the demo credentials configured for the local dummy client and internal IDP setup.

The exact demo values come from the runtime env rendered by the seeder and the seeded DynamoDB data.

## Event Mode (ECS Autoscaling) 🚀

One Identity supports an **Event Mode** to handle high-traffic periods by increasing ECS autoscaling limits and forcing a higher number of tasks.

### Activation/Deactivation Workflow

Activation and deactivation must be performed via Pull Request:

1. **Open a PR**: In the target environment (e.g., `src/infra/prod/eu-south-1`), set the `event_mode` variable to `true`.
2. **Adjust Scale**: If the default `event_autoscaling` values are not sufficient for the specific event, update them in the same PR.
3. **Merge and Apply**: Once the PR is merged, the infrastructure is updated.
4. **Cleanup**: After the event, open a new PR to set `event_mode` back to `false` to return to normal operating limits.

### Configuration per Environment

| Environment | Normal (min-max/desired) | Event (min-max/desired) |
|-------------|-------------------------|-------------------------|
| **PROD**    | 3-12 / 3                | 40-1000 / 40            |
| **UAT**     | 1-3 / 1                 | 10-50 / 10              |
| **DEV**     | 1-2 / 1                 | 3-12 / 3                |

> **Note:** The `event_mode` flag updates the autoscaling min/max capacity via the ECS module and forces the `desired_count` using an AWS CLI workaround within a `null_resource`.

# Code of Conduct
For a comprehensive Code of Conduct please refer to the [PagoPA's one](https://github.com/pagopa/ospo-utils/blob/main/common/CODE_OF_CONDUCT.md). 


# Licensing
This project is licensed under the terms of the [Mozilla Public License Version 2.0](LICENSE).
