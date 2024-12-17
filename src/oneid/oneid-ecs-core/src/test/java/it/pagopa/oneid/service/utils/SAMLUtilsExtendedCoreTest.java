package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.model.Base64SAMLResponses.CORRECT_SAML_RESPONSE_01;
import static it.pagopa.oneid.model.Base64SAMLResponses.INVALID_SIGNATURE_SAML_RESPONSE_04;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_ASSERTION_UNCORRECT_FORMAT_SAML_RESPONSE_38;
import static it.pagopa.oneid.model.Base64SAMLResponses.UNSIGNED_ASSERTION_SAML_RESPONSE_03;
import static it.pagopa.oneid.model.Base64SAMLResponses.UNSIGNED_SAML_RESPONSE_02;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.IDPStatus;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.service.mock.X509CredentialTestProfile;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;

@QuarkusTest
@TestProfile(X509CredentialTestProfile.class)
public class SAMLUtilsExtendedCoreTest {

  private static final String IDP_URL = "https://localhost:8443";

  @Inject
  SAMLUtilsExtendedCore samlUtilsExtendedCore;

  @ConfigProperty(name = "entity_id")
  String ENTITY_ID;

  @Inject
  MarshallerFactory marshallerFactory;


  @Test
  void buildIssuer() {
    Issuer issuer = samlUtilsExtendedCore.buildIssuer();
    assertNotNull(issuer);
    assertEquals(ENTITY_ID, issuer.getValue());
    assertEquals(ENTITY_ID, issuer.getNameQualifier());
    assertEquals(NameIDType.ENTITY, issuer.getFormat());
  }

  @Test
  void buildNameIdPolicy() {
    NameIDPolicy nameIDPolicy = samlUtilsExtendedCore.buildNameIdPolicy();
    assertNotNull(nameIDPolicy);
    assertEquals(NameIDType.TRANSIENT, nameIDPolicy.getFormat());
  }

  @ParameterizedTest
  @ValueSource(strings = {"spidLevelTest"})
  @NullAndEmptySource
  void buildRequestedAuthnContext(String spidLevel) {
    RequestedAuthnContext requestedAuthnContext = samlUtilsExtendedCore.buildRequestedAuthnContext(
        spidLevel);

    assertNotNull(requestedAuthnContext);
    assertEquals(AuthnContextComparisonTypeEnumeration.MINIMUM,
        requestedAuthnContext.getComparison());
    if (StringUtils.isBlank(spidLevel)) {
      assertNull(requestedAuthnContext.getAuthnContextClassRefs().getFirst().getURI());
    } else {
      assertEquals(spidLevel, requestedAuthnContext.getAuthnContextClassRefs().getFirst().getURI());
    }

  }

  @Test
  @SneakyThrows
  void getAttributeDTOListFromAssertion_withAttributeValues() {
    Response samlResponse = samlUtilsExtendedCore.getSAMLResponseFromString(
        CORRECT_SAML_RESPONSE_01);
    Assertion assertion = samlResponse.getAssertions().getFirst();

    assertNotNull(samlUtilsExtendedCore.getAttributeDTOListFromAssertion(assertion));
    assertEquals("dateOfBirth",
        samlUtilsExtendedCore.getAttributeDTOListFromAssertion(assertion).get().getFirst()
            .getAttributeName());
    assertEquals("2000-01-01",
        samlUtilsExtendedCore.getAttributeDTOListFromAssertion(assertion).get().getFirst()
            .getAttributeValue());

  }

  @Test
  void getAttributeDTOListFromAssertion_withEmptyAttributeValues() {
    Assertion assertion = mock(Assertion.class);
    assertTrue(assertion.getAttributeStatements().isEmpty());
    assertTrue(samlUtilsExtendedCore.getAttributeDTOListFromAssertion(assertion).isEmpty());

  }

  @Test
  @SneakyThrows
  void getSAMLResponseFromString_success() {

    String samlResponseString = CORRECT_SAML_RESPONSE_01;

    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        samlResponseString);

    assertNotNull(response);
    assertDoesNotThrow(() -> {
      samlUtilsExtendedCore.getSAMLResponseFromString(
          samlResponseString);
    });
  }

  @Test
  @SneakyThrows
  void getSAMLResponseFromString_unMarshallingException() {

    String samlResponseString = ISSUE_INSTANT_ASSERTION_UNCORRECT_FORMAT_SAML_RESPONSE_38;

    OneIdentityException exception = assertThrows(OneIdentityException.class,
        () -> samlUtilsExtendedCore.getSAMLResponseFromString(samlResponseString));
    assertEquals(UnmarshallingException.class, exception.getCause().getClass());
  }

  @Test
  @SneakyThrows
  void getSAMLResponseFromString_IllegalArgumentException() {
    String response =
        "Invalid SAML response";

    OneIdentityException exception = assertThrows(OneIdentityException.class,
        () -> samlUtilsExtendedCore.getSAMLResponseFromString(response));
    assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
  }

  @Test
  @SneakyThrows
  void validateSignature_success() {
    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        CORRECT_SAML_RESPONSE_01);

    IDP testIDP = IDP.builder()
        .entityID("https://validator.dev.oneid.pagopa.it")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    assertDoesNotThrow(() -> samlUtilsExtendedCore.validateSignature(response,
        testIDP));

  }

  @Test
  @SneakyThrows
  void validateSignature_UsignedSamlResponseException() {
    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        UNSIGNED_SAML_RESPONSE_02);

    IDP testIDP = IDP.builder()
        .entityID("https://validator.dev.oneid.pagopa.it")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    SAMLValidationException exception = assertThrows(SAMLValidationException.class,
        () -> samlUtilsExtendedCore.validateSignature(response, testIDP));
    assertEquals(SAMLValidationException.class, exception.getClass());

  }

  @Test
  @SneakyThrows
  void validateSignature_UnsignedAssertionException() {
    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        UNSIGNED_ASSERTION_SAML_RESPONSE_03);

    IDP testIDP = IDP.builder()
        .entityID("https://validator.dev.oneid.pagopa.it")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    SAMLValidationException exception = assertThrows(SAMLValidationException.class,
        () -> samlUtilsExtendedCore.validateSignature(response, testIDP));
    assertEquals(SAMLValidationException.class, exception.getClass());
    assertEquals("Assertion signature not present", exception.getMessage());

  }

  @Test
  @SneakyThrows
  void validateSignature_AssertionSignatureValidationException() {
    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        INVALID_SIGNATURE_SAML_RESPONSE_04);

    IDP testIDP = IDP.builder()
        .entityID("https://validator.dev.oneid.pagopa.it")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    SAMLValidationException exception = assertThrows(SAMLValidationException.class,
        () -> samlUtilsExtendedCore.validateSignature(response, testIDP));
    assertEquals("Response invalid signature",
        exception.getMessage());

  }

}
