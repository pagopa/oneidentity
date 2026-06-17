package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
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
}
