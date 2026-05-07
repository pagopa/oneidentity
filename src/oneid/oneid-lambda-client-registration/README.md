## Local Development

Run all commands from the Maven workspace root:

```shell
cd src/oneid
```

### Purpose

`oneid-lambda-client-registration` is the Quarkus service responsible for client registration APIs.

The controller exposes routes under `/oidc`, including:

- `/oidc/register`
- `/oidc/register/user_id/{user_id}`
- `/oidc/register/client_id/{client_id}`
- `/oidc/register/plan-list`
- `/oidc/register/validate-api-key`
- `/oidc/clients/{client_id}/secret/refresh`

In the default local compose stack it is exposed on:

- `http://localhost:8081`

### Recommended workflow

The default local compose stack already includes this service and builds it with `SKIP_DEPCHECK=true`:

```shell
cd src/oneid
docker compose up --build oneid-lambda-client-registration
```

If you want the full application flow, run the entire stack instead:

```shell
cd src/oneid
docker compose up --build
```

### Run in Quarkus dev mode

This service depends on local AWS-compatible resources. The simplest path is to start MiniStack and the seeder first:

```shell
cd src/oneid
docker compose up oneid-aws-local oneid-services-seeder
```

The current dev profile uses a local DynamoDB endpoint at `http://localhost:8000`, while the shared compose stack exposes MiniStack on `http://localhost:4566`.

If you want to use the compose-provided MiniStack from the host, override the DynamoDB endpoint when starting dev mode:

```shell
cd src/oneid
./mvnw quarkus:dev \
  -P oneid-lambda-client-registration-aggregate \
  -DskipDepcheck=true \
  -Dquarkus.dynamodb.endpoint-override=http://localhost:4566
```

If you want depcheck enabled, provide your Maven settings file:

```shell
cd src/oneid
./mvnw quarkus:dev \
  -P oneid-lambda-client-registration-aggregate \
  -Dquarkus.dynamodb.endpoint-override=http://localhost:4566 \
  -s settings.xml
```

### Docker image build

Fast local build without depcheck:

```shell
cd src/oneid
docker build -f oneid-lambda-client-registration/Dockerfile --build-arg SKIP_DEPCHECK=true -t local/oneid-lambda-client-registration .
```

Depcheck-enabled build with BuildKit secret:

```shell
cd src/oneid
DOCKER_BUILDKIT=1 docker build \
  --secret id=maven_settings,src=settings.xml \
  -f oneid-lambda-client-registration/Dockerfile \
  -t local/oneid-lambda-client-registration .
```

Current `skipDepcheck` behavior is:

- default: depcheck enabled
- `-DskipDepcheck=false`: depcheck enabled
- `-DskipDepcheck=true`: depcheck disabled

### Useful Commands

Follow logs:

```shell
cd src/oneid
docker compose logs -f oneid-lambda-client-registration
```

Check the running container from the host:

```shell
curl -fsS http://localhost:8081/oidc/register/plan-list
```

## Registration API

After receiving a registration API Key for the environment you need, you can
register
your product using the following API.

```bash
curl --location 'https://{ENV}.oneid.pagopa.it/oidc/register' \
--header 'accept: */*' \
--header 'x-api-key: {REGISTRATION_API_KEY}' \
--header 'Content-Type: application/json' \
--data-raw '{
  "client_name": "{CLIENT_FRIENDLY_NAME}",
  "policy_uri": "{POLICY_URI}",
  "tos_uri": "{TOS_URI}",
  "logo_uri": "{CLIENT_LOGO_PNG}",
  "default_acr_values": ["{ACR_VALUE}"],
  "redirect_uris": ["{REDIRECT_URI_1}", "{REDIRECT_URI_2}", "{REDIRECT_URI_N}"],
  "saml_requested_attributes": ["{SAML_ATTRIBUTE_1}", "{SAML_ATTRIBUTE_2}", "{SAML_ATTRIBUTE_N}"]
}'
```

Example of API Call:

```bash
curl --location 'https://dev.oneid.pagopa.it/oidc/register' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--header 'x-api-key: {REGISTRATION_API_KEY}' \
--data-raw '{
  "client_name": "TestClient",
  "policy_uri": "http://test.com/policy.html",
  "tos_uri": "http://test.com/tos.html",
  "logo_uri": "http://test.com/logo.png",
  "default_acr_values": ["https://www.spid.gov.it/SpidL2"],
  "redirect_uris": ["https://test.com/client/cb"],
  "saml_requested_attributes": ["spidCode", "fiscalNumber"]
}'
```

Example of response:

```json
{
  "redirect_uris": [
    "https://test.com/client/cb"
  ],
  "client_name": "TestClient",
  "policy_uri": "http://test.com/policy.html",
  "tos_uri": "http://test.com/tos.html",
  "logo_uri": "http://test.com/logo.png",
  "default_acr_values": [
    "https://www.spid.gov.it/SpidL2"
  ],
  "saml_requested_attributes": [
    "spidCode",
    "fiscalNumber"
  ],
  "client_id": "mlssaurk6dx_Z4j2n9b8vgkmqjhnrcW2numEb2b5g_o",
  "client_secret": "JZcyDfkN2jp8P/Sdi8UceoI4P3UnRjFGrGm/q4Kak/U",
  "client_id_issued_at": 1722329034581,
  "client_secret_expires_at": 0
}
```

<details>
<summary style="font-size:1.17em; font-weight:bold;">ENV Values</summary>

List of possible ENV values

| Environment |
|-------------|
| dev         |
| uat         |
| prod        |

</details>
<details>
<summary style="font-size:1.17em; font-weight:bold;">ACR Values</summary>

List of possible ACR values

| ACR                            |
|--------------------------------|
| https://www.spid.gov.it/SpidL2 |
| https://www.spid.gov.it/SpidL3 |

</details>
<details>
<summary style="font-size:1.17em; font-weight:bold;">SAML Requested Attributes</summary>

List of possible SAML Requested Attributes

| Attribute          |
|--------------------|
| spidCode           |
| name               |
| familyName         |
| placeOfBirth       |
| countyOfBirth      |
| dateOfBirth        |
| gender             |
| companyName        |
| registeredOffice   |
| fiscalNumber       |
| ivaCode            |
| idCard             |
| mobilePhone        |
| email              |
| address            |
| expirationDate     |
| digitalAddress     |
| domicileAddress    |
| domicilePlace      |
| domicilePostalCode |
| domicileProvince   |
| domicileCountry    |
| qualification      |
| commonName         |
| surname            |
| givenName          |
| preferredUsername  |
| title              |
| userCertificate    |
| employeeNumber     |
| orgUnitName        |
| preferredLanguage  |
| country            |
| stateOrProvince    |
| city               |
| postalCode         |
| street             |

</details>


After the registration, you can retrieve the information saved using the client_id obtained with the
following API:

```bash
curl --location 'https://dev.oneid.pagopa.it/oidc/register/{CLIENT_ID}'
```

Example of API call:

```bash
curl --location 'https://dev.oneid.pagopa.it/oidc/register/PkWUAK99cca6MQ0QtC91Qsff6h5hMFb1bXsz9mpGd94'
```

Example of response:

```json
{
  "redirect_uris": [
    "https://442zl6z6sbdqprefkazmp6dr3y0nmnby.lambda-url.eu-south-1.on.aws/client/cb"
  ],
  "client_name": "test_test",
  "policy_uri": "http://test.com/policy.html",
  "tos_uri": "http://test.com/tos.html",
  "logo_uri": "http://test.com/logo.png",
  "default_acr_values": [
    "https://www.spid.gov.it/SpidL2"
  ],
  "saml_requested_attributes": [
    "spidCode"
  ]
}
```