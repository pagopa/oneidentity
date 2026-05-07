# oneid-ecs-core

## Purpose

`oneid-ecs-core` is the main Quarkus backend for the local One Identity stack.

It exposes the core HTTP routes used by the application, including:

- OIDC endpoints under `/oidc`
- SAML endpoints under `/saml`
- client and IDP endpoints such as `/clients` and `/idps`

In the default local stack, it runs behind the nginx gateway exposed on `http://localhost:8080`.

## Local Development

Run all commands from the Maven workspace root:

```shell
cd src/oneid
```

### Prerequisites

- JDK 21
- Docker
- Docker Compose

### Recommended workflow

For the most complete local experience, start the compose stack:

```shell
cd src/oneid
docker compose up --build
```

The compose stack starts:

- local AWS-compatible services on `http://localhost:4566`
- the local seeder that creates DynamoDB tables, KMS alias, and SSM parameters
- `oneid-ecs-core`
- `oneid-ecs-internal-idp`
- frontend and gateway services

The normal browser entry point is:

- `http://localhost:8080`

### Run only `oneid-ecs-core` in dev mode

If you want to run the service outside Docker while keeping local dependencies available, first start MiniStack and the seeder:

```shell
cd src/oneid
docker compose up oneid-aws-local oneid-services-seeder
```

Then start Quarkus dev mode:

```shell
cd src/oneid
./mvnw quarkus:dev -P oneid-ecs-core-aggregate -DskipDepcheck=true
```

If you want depcheck enabled instead of skipped, provide Maven settings with GitHub Packages credentials:

```shell
cd src/oneid
./mvnw quarkus:dev -P oneid-ecs-core-aggregate -s settings.xml
```

### Docker image build

The Dockerfile supports two local build modes.

Fast local build without depcheck:

```shell
cd src/oneid
docker build -f oneid-ecs-core/Dockerfile --build-arg SKIP_DEPCHECK=true -t local/oneid-ecs-core .
```

Depcheck-enabled build with BuildKit secret:

```shell
cd src/oneid
DOCKER_BUILDKIT=1 docker build \
    --secret id=maven_settings,src=settings.xml \
    -f oneid-ecs-core/Dockerfile \
    -t local/oneid-ecs-core .
```

Current `skipDepcheck` behavior is:

- default: depcheck enabled
- `-DskipDepcheck=false`: depcheck enabled
- `-DskipDepcheck=true`: depcheck disabled

The local compose stack passes `SKIP_DEPCHECK=true` for this service so local container builds do not depend on GitHub Packages credentials.

## Local Dependencies

The local stack seeds and refreshes the resources used by `oneid-ecs-core`:

- `SSM` parameters `cert.pem` and `key.pem`
- DynamoDB tables used by the authentication flows
- KMS alias `alias/sign-jwt`

If you reseed certificates or delete local `SSM` parameters while the service is already running, restart the service so it reloads credentials:

```shell
cd src/oneid
docker compose restart oneid-ecs-core
```

## Useful Commands

Follow service logs:

```shell
cd src/oneid
docker compose logs -f oneid-ecs-core
```

Check health through the container port:

```shell
curl -fsS http://localhost:8080/q/health
```

## Notes

- `oneid-ecs-core` is built from the workspace root `src/oneid/pom.xml` with the aggregate profile `oneid-ecs-core-aggregate`.
- The root repository README contains the complete stack-level local-development guide.
