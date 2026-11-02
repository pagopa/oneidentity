package it.pagopa.oneid.service;

import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_MATCHING_FOR_CIE;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ISSUER_MISMATCH;
import static it.pagopa.oneid.model.Base64SAMLResponses.CIE_EXTRA_INVALID_ATTRIBUTE_SAMLRESPONSE;
import static it.pagopa.oneid.model.Base64SAMLResponses.CIE_VALID_SAMLRESPONSE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectSpy;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.session.enums.AuthnContextComparisonType;
import it.pagopa.oneid.service.mock.X509CredentialTestProfile;
import it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opensaml.saml.saml2.core.Response;
import software.amazon.awssdk.services.s3.S3Client;

@QuarkusTest
@TestProfile(SAMLServiceCieTest.CieTestProfile.class)
class SAMLServiceCieTest {

  private static final String PREPRODUCTION =
      "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO";
  private static final String TESTING =
      "https://collaudo.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO";

  public static class CieTestProfile extends X509CredentialTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          "cie_entity_id", PREPRODUCTION,
          "cie_additional_entity_ids", PREPRODUCTION + "," + TESTING);
    }
  }

  @Inject
  SAMLServiceImpl samlService;
  @InjectSpy
  SAMLUtilsExtendedCore samlUtils;
  @InjectSpy
  Clock clock;
  @InjectMock
  IDPConnectorImpl idpConnector;
  @InjectMock
  S3Client s3Client;
  @InjectMock
  CloudWatchConnectorImpl cloudWatchConnector;

  @ParameterizedTest
  @ValueSource(strings = {PREPRODUCTION, TESTING})
  @DisplayName("Both configured CIE providers use the current CIE metadata")
  void given_configured_cie_when_resolving_provider_then_use_cie_metadata(String entityId) {
    IDP idp = IDP.builder().entityID(entityId).build();
    when(idpConnector.getIDPByEntityIDAndTimestamp(entityId, "LATEST_CIE"))
        .thenReturn(Optional.of(idp));

    assertEquals(Optional.of(idp), samlService.getIDPFromEntityID(entityId));
    verify(idpConnector).getIDPByEntityIDAndTimestamp(entityId, "LATEST_CIE");
  }

  @ParameterizedTest
  @ValueSource(strings = {PREPRODUCTION, TESTING})
  @DisplayName("Both CIE providers retain issuer-format exceptions and the CIE attribute set")
  void given_valid_cie_response_when_validating_then_accept(String entityId)
      throws OneIdentityException {
    Response response = cieResponse(CIE_VALID_SAMLRESPONSE, entityId);
    response.getIssuer().setFormat("unspecified");
    response.getAssertions().getFirst().getIssuer().setFormat(null);
    IDP idp = IDP.builder().entityID(entityId).build();
    when(idpConnector.getIDPByEntityIDAndTimestamp(entityId, "LATEST_CIE"))
        .thenReturn(Optional.of(idp));
    doNothing().when(samlUtils).validateSignature(any(), any());

    assertDoesNotThrow(() -> validate(response, entityId));
    verify(samlUtils).validateSignature(response, idp);
  }

  @ParameterizedTest
  @ValueSource(strings = {PREPRODUCTION, TESTING})
  @DisplayName("Both CIE providers reject unexpected attributes")
  void given_extra_cie_attributes_when_validating_then_reject(String entityId)
      throws OneIdentityException {
    Response response = cieResponse(CIE_EXTRA_INVALID_ATTRIBUTE_SAMLRESPONSE, entityId);

    SAMLValidationException error = assertThrows(SAMLValidationException.class,
        () -> validate(response, entityId));

    assertTrue(error.getMessage().contains(
        IDP_ERROR_ATTRIBUTES_NOT_MATCHING_FOR_CIE.getErrorMessage()));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  @DisplayName("Selecting one CIE provider never accepts the other provider's issuer")
  void given_mismatched_cie_issuer_when_validating_then_reject(boolean assertionIssuer)
      throws OneIdentityException {
    Response response = cieResponse(CIE_VALID_SAMLRESPONSE, TESTING);
    if (assertionIssuer) {
      response.getAssertions().getFirst().getIssuer().setValue(PREPRODUCTION);
    } else {
      response.getIssuer().setValue(PREPRODUCTION);
    }

    SAMLValidationException error = assertThrows(SAMLValidationException.class,
        () -> validate(response, TESTING));

    assertTrue(error.getMessage().contains(IDP_ERROR_ISSUER_MISMATCH.getErrorMessage()));
  }

  private Response cieResponse(String encodedResponse, String entityId)
      throws OneIdentityException {
    Response response = samlUtils.getSAMLResponseFromString(encodedResponse);
    response.getIssuer().setValue(entityId);
    response.getAssertions().getFirst().getIssuer().setValue(entityId);
    when(clock.instant()).thenReturn(response.getIssueInstant().plusMillis(10));
    return response;
  }

  private void validate(Response response, String entityId) {
    Instant requestInstant = response.getIssueInstant().minusSeconds(10);
    samlService.validateSAMLResponse(response, entityId, Set.of("spidCode", "fiscalNumber"),
        requestInstant, AuthLevel.L2, AuthnContextComparisonType.MINIMUM,
        "https://client.example/cb", "test-state", "test-client", null);
  }
}
