package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.connector.InternalIDPUsersConnectorImpl;
import it.pagopa.oneid.exception.MalformedAuthnRequestException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.IDPInternalUser;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.SsmException;

@QuarkusTest
@TestProfile(InternalIDPServiceTestProfile.class)
class InternalIDPServiceImplTest {

  @Inject
  InternalIDPServiceImpl internalIDPServiceImpl;

  @InjectMock
  InternalIDPUsersConnectorImpl internalIDPUsersConnectorImpl;

  @InjectMock
  ClientConnectorImpl clientConnectorImpl;

  @InjectMock
  SsmClient ssmClient;

  @Test
  void getAuthnRequestFromString() {
  }

  @Test
  @DisplayName("given_malformed_redirect_request_when_getAuthnRequestFromRedirectString_then_throws")
  void getAuthnRequestFromRedirectString_malformed() {
    assertThrows(MalformedAuthnRequestException.class,
        () -> internalIDPServiceImpl.getAuthnRequestFromRedirectString("invalid-base64"));
  }

  @Test
  @DisplayName("given_valid_redirect_signature_when_validateRedirectSignature_then_no_exception")
  void validateRedirectSignature_validSignature() throws SAMLUtilsException {
    String samlRequest = "encodedSamlRequest";
    String relayState = "relay-state";
    String sigAlg = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512;

    String queryString = internalIDPServiceImpl.buildRedirectQueryString(samlRequest, relayState,
        sigAlg);
    String signature = internalIDPServiceImpl.signRedirectQueryString(queryString);

    assertDoesNotThrow(() -> internalIDPServiceImpl.validateRedirectSignature(samlRequest,
        relayState, sigAlg, signature));
  }

  @Test
  @DisplayName("given_invalid_redirect_signature_when_validateRedirectSignature_then_throws")
  void validateRedirectSignature_invalidSignature() {
    assertThrows(SAMLValidationException.class,
        () -> internalIDPServiceImpl.validateRedirectSignature("encodedSamlRequest", "relay",
            SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512, "invalid-signature"));
  }

  @Test
  @DisplayName("given_unsigned_redirect_authn_request_when_validateRedirectAuthnRequest_then_no_exception")
  void validateRedirectAuthnRequest_unsignedRequest() {
    AuthnRequest authnRequest = internalIDPServiceImpl.buildSAMLObject(AuthnRequest.class);
    authnRequest.setID("test-request-id");
    authnRequest.setIssueInstant(Instant.now());
    authnRequest.setAttributeConsumingServiceIndex(0);
    authnRequest.setDestination("https://localhost/samlsso");

    Issuer issuer = internalIDPServiceImpl.buildSAMLObject(Issuer.class);
    issuer.setValue("https://localhost/issuer");
    authnRequest.setIssuer(issuer);

    assertDoesNotThrow(() -> internalIDPServiceImpl.validateRedirectAuthnRequest(authnRequest));
  }

  @Test
  void validateAuthnRequest() {
  }

  @Test
  void getClientByAttributeConsumingServiceIndex() {
  }

  @Test
  @DisplayName("given_null_age_params_when_verifyAge_then_no_exception")
  void verifyAge_nullParams() {
    assertDoesNotThrow(
        () -> internalIDPServiceImpl.verifyAge("clientId", "username", null, null, null));
  }

  @Test
  @DisplayName("given_user_within_age_range_when_verifyAge_then_no_exception")
  void verifyAge_withinRange() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Mario"))
        .age(20)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    assertDoesNotThrow(
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99, null));
  }

  @Test
  @DisplayName("given_user_below_min_age_when_verifyAge_then_throws")
  void verifyAge_belowMinAge() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Mario"))
        .age(10)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    OneIdentityException ex = assertThrows(OneIdentityException.class,
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99, null));
    assertTrue(ex.getMessage().contains("Spiacente Mario"));
  }

  @Test
  @DisplayName("given_user_above_max_age_when_verifyAge_then_throws")
  void verifyAge_aboveMaxAge() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Luigi"))
        .age(25)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    OneIdentityException ex = assertThrows(OneIdentityException.class,
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 18, null));
    assertTrue(ex.getMessage().contains("Spiacente Luigi"));
  }

  @Test
  @DisplayName("given_max_age_99_and_user_older_when_verifyAge_then_no_exception")
  void verifyAge_maxAge99MeansNoUpperLimit() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Nonna"))
        .age(80)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    assertDoesNotThrow(
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99, null));
  }

  @Test
  @DisplayName("given_user_not_found_when_verifyAge_then_throws")
  void verifyAge_userNotFound() {
    Mockito.when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.empty());

    OneIdentityException ex = assertThrows(OneIdentityException.class,
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99, null));
    assertEquals("User not found", ex.getMessage());
  }

  @Test
  @DisplayName("given_user_without_age_when_verifyAge_then_no_exception")
  void verifyAge_missingAge() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Mario"))
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    assertDoesNotThrow(
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99, null));
  }

  @Test
  @DisplayName("given_user_below_ageParentAuth_when_verifyAge_then_throws")
  void verifyAge_belowAgeParentAuth() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Mario"))
        .age(15)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    OneIdentityException ex = assertThrows(OneIdentityException.class,
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99, 16));
    assertTrue(ex.getMessage().contains("autorizzazione dei genitori"));
  }

  @Test
  @DisplayName("given_user_at_or_above_ageParentAuth_when_verifyAge_then_no_exception")
  void verifyAge_atOrAboveAgeParentAuth() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Mario"))
        .age(16)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    assertDoesNotThrow(
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99, 16));
  }

  @Test
  @DisplayName("given_ageParentAuth_zero_when_verifyAge_then_no_exception")
  void verifyAge_ageParentAuthZero() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Mario"))
        .age(15)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    assertDoesNotThrow(
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99, 0));
  }

  @Test
  @DisplayName("given_ssm_failure_when_createConsentDeniedSamlResponse_then_throws_runtime_exception")
  void createConsentDeniedSamlResponse_ssmFailure_throwsRuntimeException() {
    Mockito.when(ssmClient.getParameter(Mockito.any(GetParameterRequest.class)))
        .thenThrow(SsmException.builder().message("SSM error").build());

    assertThrows(RuntimeException.class,
        () -> internalIDPServiceImpl.createConsentDeniedSamlResponse("testAuthnRequestId"));
  }

  @Test
  @DisplayName("given_invalid_certificate_when_createConsentDeniedSamlResponse_then_throws_runtime_exception")
  void createConsentDeniedSamlResponse_invalidCertificate_throwsRuntimeException() {
    // SSM get not valid certificate
    GetParameterResponse invalidCertResponse = GetParameterResponse.builder()
        .parameter(Parameter.builder().value("not-a-real-cert").build())
        .build();
    Mockito.when(ssmClient.getParameter(Mockito.any(GetParameterRequest.class)))
        .thenReturn(invalidCertResponse);

    assertThrows(RuntimeException.class,
        () -> internalIDPServiceImpl.createConsentDeniedSamlResponse("testAuthnRequestId"));
  }

  @Test
  @DisplayName("given_cert_ok_but_invalid_key_when_createConsentDeniedSamlResponse_then_throws_runtime_exception")
  void createConsentDeniedSamlResponse_invalidKey_throwsRuntimeException() {
    // first getParameter (cert) ok, second getParameter (key) not valid
    String testCertPem = "-----BEGIN CERTIFICATE-----\n"
        + "MIIBpDCCAQ2gAwIBAgIUYz1234InvalidTestCertForUnitTests1234567890MA0G"
        + "CSqGSIb3DQEBCwUAMCMxITAfBgNVBAMTGEludGVybmFsSURQVGVzdENlcnQwHhcN\n"
        + "-----END CERTIFICATE-----";
    GetParameterResponse certResponse = GetParameterResponse.builder()
        .parameter(Parameter.builder().value(testCertPem).build())
        .build();
    GetParameterResponse invalidKeyResponse = GetParameterResponse.builder()
        .parameter(Parameter.builder().value("not-a-real-key").build())
        .build();
    Mockito.when(ssmClient.getParameter(Mockito.any(GetParameterRequest.class)))
        .thenReturn(certResponse)
        .thenReturn(invalidKeyResponse);

    assertThrows(RuntimeException.class,
        () -> internalIDPServiceImpl.createConsentDeniedSamlResponse("testAuthnRequestId"));
  }

  private IDPInternalUser buildTestUser(String username, String clientId) {
    return IDPInternalUser.builder()
        .username(username)
        .namespace(clientId)
        .password("pass")
        .samlAttributes(Map.of("fiscalNumber", "RSSMRA80A01H501U"))
        .build();
  }

  private Client buildTestClient(String clientId, AuthLevel authLevel) {
    return Client.builder()
        .clientId(clientId)
        .authLevel(authLevel)
        .requestedParameters(java.util.Set.of("fiscalNumber"))
        .callbackURI(java.util.Set.of("https://localhost/cb"))
        .friendlyName("Test Client")
        .acsIndex(0)
        .attributeIndex(0)
        .isActive(true)
        .clientIdIssuedAt(0L)
        .build();
  }

  // region SSM mock — self-signed RSA cert+key (openssl req -x509 / pkcs8,
  // test-only) // gitleaks:allow
  private static final String TEST_CERT_PEM = "-----BEGIN CERTIFICATE-----\n" // gitleaks:allow
      + "MIIDBTCCAe2gAwIBAgIUa+M221BaG6NKPx8OSrKjqE4tvYAwDQYJKoZIhvcNAQEL\n"
      + "BQAwEjEQMA4GA1UEAwwHVGVzdElEUDAeFw0yNjA4MDUxMjMzMzlaFw0zNjA4MDIx\n"
      + "MjMzMzlaMBIxEDAOBgNVBAMMB1Rlc3RJRFAwggEiMA0GCSqGSIb3DQEBAQUAA4IB\n"
      + "DwAwggEKAoIBAQCvyidJ8iaM93ymiTqH/GOZWzU7/bR6bYMK/czHiHZNLgmUARgE\n"
      + "1rXWxsrQ5vrnVgHKkg49+AX84L+BAjh/e4djzOquRWn/arw8oo7TWmmqFG1emkLD\n"
      + "5qNqENdIRp22QdAd61FaXEOfhiKs24LqzKNl6W1s3JXEB0hVjw8Bc5IkjDBz8zgN\n"
      + "Cz2qViG3bZ7UAbSQowGwd8adMnt/D3GrKgt1FR+mZZHNCui0DEl4wsqX8CuYwy5m\n"
      + "NmI+ui5Sq/VDeNhiMAvew+tmAcz4rGJWbsmF8NEufSS52iGBOLOfgcKDq4yZ/CTL\n"
      + "XLhs9LIOOMejZcESMM69S3XE7mgkRumPJPshAgMBAAGjUzBRMB0GA1UdDgQWBBRt\n"
      + "QxlUV3gH182o6L6ooYCsA4mOeDAfBgNVHSMEGDAWgBRtQxlUV3gH182o6L6ooYCs\n"
      + "A4mOeDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQATJv9Rbz7L\n"
      + "E5iRxLaCttlVWDoPDKaKKLS2/W+knhnlO9dp6guNMg6ZdNj6JgYNktkqFRqFDwR+\n"
      + "upWtBmY/IJboNavfbGiGOsg45H9scf9Iq0uH8zPCJKxJAfbQyqQ1zwTDWMeXkmEy\n"
      + "mublHy0rlIrFRbjHpi05e6q2u2Efe2HEW0IVnmdXK44AJUu9umf2Zhy+iY/HBwm3\n"
      + "jVmp1nU/Blm4ojXDR/oa0o+JRGuXFvJyKmODQQQoul5wME/p5TKw5JSX68t870o+\n"
      + "8gG8sjBqWagKI9VsO5yBlEKGHjIlSmNjNEh5GfHwehMMAb0kO467MxuBP0iuNIhQ\n"
      + "Bb+3Jm9l+t+3\n"
      + "-----END CERTIFICATE-----";

  private static final String TEST_KEY_PEM = "-----BEGIN PRIVATE KEY-----\n" // gitleaks:allow
      + "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCvyidJ8iaM93ym\n"
      + "iTqH/GOZWzU7/bR6bYMK/czHiHZNLgmUARgE1rXWxsrQ5vrnVgHKkg49+AX84L+B\n"
      + "Ajh/e4djzOquRWn/arw8oo7TWmmqFG1emkLD5qNqENdIRp22QdAd61FaXEOfhiKs\n"
      + "24LqzKNl6W1s3JXEB0hVjw8Bc5IkjDBz8zgNCz2qViG3bZ7UAbSQowGwd8adMnt/\n"
      + "D3GrKgt1FR+mZZHNCui0DEl4wsqX8CuYwy5mNmI+ui5Sq/VDeNhiMAvew+tmAcz4\n"
      + "rGJWbsmF8NEufSS52iGBOLOfgcKDq4yZ/CTLXLhs9LIOOMejZcESMM69S3XE7mgk\n"
      + "RumPJPshAgMBAAECggEAHOQZjvKoWVS3fNti7kSRzfuKhUCOL+zds6nAxS5tWwPs\n"
      + "+REw9GKhYhor7Sd5Jxogkz1r781alpQNhVNrG7CesM1V6Kxp3luZclG5LY1Xfmd7\n"
      + "DihGPtIiG2WHLE7GztSEMtag+TvetVIjUsuE+bgGRtKVdGXQi3Yy8jTf33oyE6Vc\n"
      + "ZP7r6gaMfkF18NvDVrGaDNDzBg56V8jm5A1eR1dGyI1CF1JffmxM+MG+AxPm3nFd\n"
      + "vMHhuJUf2p9iJ9OJV2DiH0aa7WHZs6W6LnVJLO9MNyMS53mzIvB6Ir0rY71hYM/o\n"
      + "+NjsZjcY4nAWxPJrr01enw7jhFmEl8IokwjTC9J8DQKBgQDxZpLm4D2X6ZQBlrrV\n"
      + "ScXHQZSYt9mNhxA0F8gXEg7EU84l8BcwA/UxiqIbQ5ZgQJz5cqqaqxHzLmZn7s2t\n"
      + "rFgXlmzASvbQISIzOCP9lBgN7L67pMga4RLEp5YmXj5GcbVD6sZNAMUWsvD7n7Nj\n"
      + "8GZTjQturKMpmn6TOYvxLYD17wKBgQC6a8X5Ee9ce3ohBfeKwlOtROhWmUZMJEsk\n"
      + "m96+wuaC0Hd4PJTnk+XixhjagLVvumf8+llbISf9FKvKl4vTfO5IYsnHaAzrmnM5\n"
      + "iWege09oqcdLsOjEDNa9DlIAJji9KjK0wthDqzssd/0r8ACDk/rXrfFVoB4LD1kH\n"
      + "3rHLR2+v7wKBgQDexgpLhiVRS/2Kec9xheM/PwYi6YebVElfyZKdt8xo4e4clLTR\n"
      + "WtWsIoP3JShhKz+x9F/83GJ0SR24TGkDHUkue2NhEc0zxWAKZITSPxjzwMdD443H\n"
      + "QG5RjVbDkbVmzQi65K6b4VoA2KILlBW45OGJuWATgr4QZsLomvLKX3PjgQKBgCPD\n"
      + "m1Z+T1qBwdtmBqiTQXRjXOOIQklHSTc7xA762zhLgDAMB1EqOZc5A/mfaa+Gpn6N\n"
      + "FOl7we02swKJ8mRfemNeuHFOD/R/TA3Ew6GxJnng4IAv72f1QplAjxDCRG5eEx5E\n"
      + "pzLMPez5FBQC1kZn9F6+WydfMYRTrHPrEFb8XHv3AoGBAN0QPbZzyCifKnBbVYGb\n"
      + "DSH06tS+TDB8J6DTHFYXlrkkvhgDocNZ2dZkf02dtx9H4N6COMnoSum1J9k1a0is\n"
      + "xecqZea5/fIwgvnGKEbnhqTul0yUcfBr5bWHb+RO9yy7jUcKVYDrCQ85hGqLZmxJ\n"
      + "Xex4+CqzwGQzzbM1muriTGFY\n"
      + "-----END PRIVATE KEY-----";
  // endregion

  private void mockSsmWithValidCredentials() {
    GetParameterResponse certResponse = GetParameterResponse.builder()
        .parameter(Parameter.builder().value(TEST_CERT_PEM).build()).build();
    GetParameterResponse keyResponse = GetParameterResponse.builder()
        .parameter(Parameter.builder().value(TEST_KEY_PEM).build()).build();
    when(ssmClient.getParameter(any(GetParameterRequest.class)))
        .thenReturn(certResponse)
        .thenReturn(keyResponse);
  }

  @Test
  @DisplayName("given_null_requestedAuthLevel_when_createSuccessfulSamlResponse_then_uses_client_default_level")
  void createSuccessfulSamlResponse_nullRequestedLevel_usesClientDefault()
      throws SAMLUtilsException {
    String clientId = "testClientId";
    Client client = buildTestClient(clientId, AuthLevel.L2);
    IDPInternalUser user = buildTestUser("testUser", clientId);

    mockSsmWithValidCredentials();
    when(clientConnectorImpl.getClientById(clientId)).thenReturn(Optional.of(client));
    when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", clientId))
        .thenReturn(Optional.of(user));

    var response = internalIDPServiceImpl.createSuccessfulSamlResponse(
        "authnReqId", clientId, "testUser", null);

    String classRef = response.getAssertions().getFirst()
        .getAuthnStatements().getFirst()
        .getAuthnContext().getAuthnContextClassRef().getURI();
    assertEquals(AuthLevel.L2.getValue(), classRef);
  }

  @Test
  @DisplayName("given_valid_L3_requestedAuthLevel_when_createSuccessfulSamlResponse_then_uses_L3")
  void createSuccessfulSamlResponse_validL3RequestedLevel_usesL3()
      throws SAMLUtilsException {
    String clientId = "testClientId";
    Client client = buildTestClient(clientId, AuthLevel.L2);
    IDPInternalUser user = buildTestUser("testUser", clientId);

    mockSsmWithValidCredentials();
    when(clientConnectorImpl.getClientById(clientId)).thenReturn(Optional.of(client));
    when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", clientId))
        .thenReturn(Optional.of(user));

    var response = internalIDPServiceImpl.createSuccessfulSamlResponse(
        "authnReqId", clientId, "testUser", AuthLevel.L3.getValue());

    String classRef = response.getAssertions().getFirst()
        .getAuthnStatements().getFirst()
        .getAuthnContext().getAuthnContextClassRef().getURI();
    assertEquals(AuthLevel.L3.getValue(), classRef);
  }

  @Test
  @DisplayName("given_invalid_requestedAuthLevel_when_createSuccessfulSamlResponse_then_falls_back_to_client_default")
  void createSuccessfulSamlResponse_invalidRequestedLevel_fallsBackToClientDefault()
      throws SAMLUtilsException {
    String clientId = "testClientId";
    Client client = buildTestClient(clientId, AuthLevel.L2);
    IDPInternalUser user = buildTestUser("testUser", clientId);

    mockSsmWithValidCredentials();
    when(clientConnectorImpl.getClientById(clientId)).thenReturn(Optional.of(client));
    when(internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace("testUser", clientId))
        .thenReturn(Optional.of(user));

    var response = internalIDPServiceImpl.createSuccessfulSamlResponse(
        "authnReqId", clientId, "testUser", "not-a-valid-level");

    String classRef = response.getAssertions().getFirst()
        .getAuthnStatements().getFirst()
        .getAuthnContext().getAuthnContextClassRef().getURI();
    assertEquals(AuthLevel.L2.getValue(), classRef);
  }
}
