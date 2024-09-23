package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.IDPStatus;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.exception.SAMLResponseStatusException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.service.utils.MetadataResolverExtended;
import it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;

@QuarkusTest
public class SAMLServiceImplTest {

  @Inject
  SAMLServiceImpl samlServiceImpl;

  @InjectSpy
  SAMLUtilsExtendedCore samlUtils;

  @Inject
  SAMLUtilsConstants samlUtilsConstants;

  @Inject
  MetadataResolverExtended metadataResolverExtended;

  @InjectMock
  IDPConnectorImpl idpConnectorImpl;

  @Test
  void buildAuthnRequest() throws OneIdentityException {
    // given
    String idpId = "dummy";
    int assertionConsumerServiceIndex = 0;
    int attributeConsumingServiceIndex = 0;
    String authLevel = "foobar";

    doReturn(Optional.of("foobar")).when(samlUtils).buildDestination(Mockito.any());
    // then

    AuthnRequest authnRequest = samlServiceImpl.buildAuthnRequest(idpId,
        assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel);

    assertFalse(authnRequest.getID().isEmpty());
  }

  @Test
  void buildAuthnRequest_idpSSONotFound() throws OneIdentityException {
    // given
    String idpId = "dummy";
    int assertionConsumerServiceIndex = 0;
    int attributeConsumingServiceIndex = 0;
    String authLevel = "foobar";

    doThrow(IDPSSOEndpointNotFoundException.class).when(samlUtils).buildDestination(Mockito.any());
    // then

    Executable executable = () -> samlServiceImpl.buildAuthnRequest(idpId,
        assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel);
    assertThrows(IDPSSOEndpointNotFoundException.class, executable);
  }

  @Test
  void buildAuthnRequest_SAMLUtilsExceptionDuringBuildDestination() throws OneIdentityException {
    // given
    String idpId = "dummy";
    int assertionConsumerServiceIndex = 0;
    int attributeConsumingServiceIndex = 0;
    String authLevel = "foobar";

    doThrow(SAMLUtilsException.class).when(samlUtils).buildDestination(Mockito.any());
    // then

    Executable executable = () -> samlServiceImpl.buildAuthnRequest(idpId,
        assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel);
    assertThrows(OneIdentityException.class, executable);
  }

  @Test
  void buildAuthnRequest_SAMLUtilsExceptionDuringBuildSignature() throws OneIdentityException {
    // given
    String idpId = "dummy";
    int assertionConsumerServiceIndex = 0;
    int attributeConsumingServiceIndex = 0;
    String authLevel = "foobar";

    doReturn(Optional.of("foobar")).when(samlUtils).buildDestination(Mockito.any());
    doThrow(SAMLUtilsException.class).when(samlUtils).buildSignature(Mockito.any());
    // then

    Executable executable = () -> samlServiceImpl.buildAuthnRequest(idpId,
        assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel);
    assertThrows(GenericAuthnRequestCreationException.class, executable);
  }

  @Test
  void checkSAMLStatus_StatusCodeSuccess() throws OneIdentityException {
    // given
    Response response = Mockito.mock(Response.class);
    Status status = Mockito.mock(Status.class);
    StatusCode statusCode = Mockito.mock(StatusCode.class);

    Mockito.when(statusCode.getValue()).thenReturn(StatusCode.SUCCESS);
    Mockito.when(status.getStatusCode()).thenReturn(statusCode);
    Mockito.when(response.getStatus()).thenReturn(status);
    // then
    assertDoesNotThrow(() -> samlServiceImpl.checkSAMLStatus(response));
  }

  @Test
  void checkSAMLStatus_StatusCodeNull() {
    // given
    Response response = Mockito.mock(Response.class);
    Status status = Mockito.mock(Status.class);

    Mockito.when(status.getStatusCode()).thenReturn(null);
    Mockito.when(response.getStatus()).thenReturn(status);
    // then
    assertThrows(OneIdentityException.class, () -> samlServiceImpl.checkSAMLStatus(response));
  }

  @Test
  void checkSAMLStatus_StatusCodeNotSuccess() {
    // given
    Response response = Mockito.mock(Response.class);
    Status status = Mockito.mock(Status.class);
    StatusCode statusCode = Mockito.mock(StatusCode.class);
    StatusMessage statusMessage = Mockito.mock(StatusMessage.class);

    Mockito.when(statusMessage.getValue()).thenReturn(String.valueOf(ErrorCode.ERRORCODE_NR20));
    Mockito.when(statusCode.getValue()).thenReturn(StatusCode.REQUESTER);
    Mockito.when(status.getStatusCode()).thenReturn(statusCode);
    Mockito.when(status.getStatusMessage()).thenReturn(statusMessage);
    Mockito.when(response.getStatus()).thenReturn(status);
    // then
    assertThrows(SAMLResponseStatusException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));
  }

  @Test
  void checkSAMLStatus_StatusCodeNotSuccess_emptyStatusMessage() {
    // given
    Response response = Mockito.mock(Response.class);
    Status status = Mockito.mock(Status.class);
    StatusCode statusCode = Mockito.mock(StatusCode.class);
    StatusMessage statusMessage = Mockito.mock(StatusMessage.class);

    Mockito.when(statusMessage.getValue()).thenReturn("");
    Mockito.when(statusCode.getValue()).thenReturn(StatusCode.REQUESTER);
    Mockito.when(status.getStatusCode()).thenReturn(statusCode);
    Mockito.when(status.getStatusMessage()).thenReturn(statusMessage);
    Mockito.when(response.getStatus()).thenReturn(status);
    // then
    assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));
  }

  @Test
  void validateSAMLResponse() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    Mockito.when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    Mockito.when(subjectConfirmationData.getRecipient()).thenReturn(SAMLUtilsConstants.ACS_URL);
    Mockito.when(
        subjectConfirmationData.getNotOnOrAfter()).thenReturn(Instant.now().plusSeconds(60));

    Mockito.when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    Mockito.when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    Mockito.when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);
    IDP testIDP = IDP.builder()
        .entityID("https://localhost:8443")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();
    Mockito.when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));

    // then
    assertDoesNotThrow(() -> samlServiceImpl.validateSAMLResponse(response, entityID));
  }

  @Test
  void validateSAMLResponse_invalidSignature() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    Mockito.when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    Mockito.when(subjectConfirmationData.getRecipient()).thenReturn(SAMLUtilsConstants.ACS_URL);
    Mockito.when(
        subjectConfirmationData.getNotOnOrAfter()).thenReturn(Instant.now().plusSeconds(60));

    Mockito.when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    Mockito.when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    Mockito.when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    doThrow(SAMLUtilsException.class)
        .when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);
    IDP testIDP = IDP.builder()
        .entityID("https://localhost:8443")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();
    Mockito.when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));

    // then
    assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID));
  }

  @Test
  void validateSAMLResponse_noAssertion() {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    List<Assertion> assertions = new ArrayList<>();
    Mockito.when(response.getAssertions()).thenReturn(assertions);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID));
  }

  @Test
  void validateSAMLResponse_nullSubjectConfirmationData() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    Mockito.when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);

    Mockito.when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(null);
    Mockito.when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    Mockito.when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID));
  }

  @Test
  void validateSAMLResponse_differentRecipient() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    Mockito.when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    Mockito.when(subjectConfirmationData.getRecipient()).thenReturn("dummy");

    Mockito.when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    Mockito.when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    Mockito.when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID));
  }

  @Test
  void validateSAMLResponse_nullRecipient() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    Mockito.when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    Mockito.when(subjectConfirmationData.getRecipient()).thenReturn(null);

    Mockito.when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    Mockito.when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    Mockito.when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID));
  }

  @Test
  void validateSAMLResponse_nullGetNotOnOrAfter() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    Mockito.when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    Mockito.when(subjectConfirmationData.getRecipient()).thenReturn(SAMLUtilsConstants.ACS_URL);
    Mockito.when(
        subjectConfirmationData.getNotOnOrAfter()).thenReturn(null);

    Mockito.when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    Mockito.when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    Mockito.when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID));
  }

  @Test
  void validateSAMLResponse_notValidGetNotOnOrAfter() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    Mockito.when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    Mockito.when(subjectConfirmationData.getRecipient()).thenReturn(SAMLUtilsConstants.ACS_URL);
    Mockito.when(
        subjectConfirmationData.getNotOnOrAfter()).thenReturn(Instant.now().minusSeconds(60));

    Mockito.when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    Mockito.when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    Mockito.when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID));
  }

  @Test
  void getSAMLResponseFromString() throws OneIdentityException {
    // given
    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Response response = Mockito.mock(Response.class);
    Mockito.when(samlUtils.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then

    assertDoesNotThrow(() -> samlServiceImpl.getSAMLResponseFromString("dummy"));
  }

  @Test
  void getAttributesFromSAMLAssertion() {
    // given
    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    AttributeDTO attributeDTO = Mockito.mock(AttributeDTO.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    Mockito.when(samlUtils.getAttributeDTOListFromAssertion(Mockito.any()))
        .thenReturn(Optional.of(List.of(attributeDTO)));

    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then

    assertDoesNotThrow(() -> samlServiceImpl.getAttributesFromSAMLAssertion(assertion));
  }

  @Test
  void getAttributesFromSAMLAssertion_emptyAttributes() {
    // given
    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    Mockito.when(samlUtils.getAttributeDTOListFromAssertion(Mockito.any()))
        .thenReturn(Optional.empty());

    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then

    assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.getAttributesFromSAMLAssertion(assertion));
  }


}
