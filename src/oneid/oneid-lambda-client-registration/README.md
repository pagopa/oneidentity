## Client Registration

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