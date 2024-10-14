package it.pagopa.oneid.service;

import static it.pagopa.oneid.model.Base64SAMLResponses.CORRECT_SAML_RESPONSE_01;
import static it.pagopa.oneid.model.Base64SAMLResponses.DESTINATION_DIFFERENT_FROM_ACS_SAML_RESPONSE_21;
import static it.pagopa.oneid.model.Base64SAMLResponses.DESTINATION_NOT_SPECIFIED_SAML_RESPONSE_19;
import static it.pagopa.oneid.model.Base64SAMLResponses.DIFFERENT_FORMAT_ATTRIBUTE_SAML_RESPONSE_30;
import static it.pagopa.oneid.model.Base64SAMLResponses.INVALID_SIGNATURE_SAML_RESPONSE_04;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUER_DIFFERENT_FROM_IDP_ENTITY_ID_SAML_RESPONSE_29;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_AFTER_REQUEST_SAML_RESPONSE_15;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_ASSERTION_AFTER_REQUEST_INSTANT_SAML_RESPONSE_40;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_ASSERTION_BEFORE_REQUEST_INSTANT_SAML_RESPONSE_39;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_ASSERTION_MISSING_SAML_RESPONSE_37;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_36;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_BEFORE_REQUEST_SAML_RESPONSE_14;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_MISSING_SAML_RESPONSE_12;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_NOT_SPECIFIED_SAML_RESPONSE_11;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_ASSERTION_ID_ATTRIBUTE_SAML_RESPONSE_34;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_ASSERTION_SAML_RESPONSE_32;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_DESTINATION_SAML_RESPONSE_20;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_FORMAT_ATTRIBUTE_SAML_RESPONSE_31;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_ISSUER_ELEMENT_SAML_RESPONSE_28;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_PRESENT_ID_SAML_RESPONSE_09;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_SPECIFIED_ASSERTION_ID_ATTRIBUTE_SAML_RESPONSE_33;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_SPECIFIED_ID_SAML_RESPONSE_08;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_SPECIFIED_ISSUER_ELEMENT_SAML_RESPONSE_27;
import static it.pagopa.oneid.model.Base64SAMLResponses.RESPONSE_IN_RESPONSE_TO_MISSING_SAML_RESPONSE_17;
import static it.pagopa.oneid.model.Base64SAMLResponses.RESPONSE_IN_RESPONSE_TO_NOT_SPECIFIED_SAML_RESPONSE_16;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_ASSERTION_METHOD_DIFFERENT_SAML_RESPONSE_55;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_ASSERTION_METHOD_MISSING_SAML_RESPONSE_54;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_ASSERTION_METHOD_NOT_SPECIFIED_SAML_RESPONSE_53;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_ASSERTION_MISSING_SAML_RESPONSE_52;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_51;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_DATA_IN_RESPONSE_TO_MISSING_SAML_RESPONSE_61;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_DATA_IN_RESPONSE_TO_NOT_SPECIFIED_SAML_RESPONSE_60;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_DATA_MISSING_SAML_RESPONSE_56;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_DATA_NOT_ON_OR_AFTER_BEFORE_RESPONSE_INSTANT_SAML_RESPONSE_66;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_DATA_NOT_ON_OR_AFTER_MISSING_SAML_RESPONSE_64;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_DATA_NOT_ON_OR_AFTER_NOT_DEFINED_SAML_RESPONSE_63;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_DATA_RECIPIENT_DIFFERENT_SAML_RESPONSE_59;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_DATA_RECIPIENT_MISSING_SAML_RESPONSE_58;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_CONFIRMATION_DATA_RECIPIENT_NOT_SPECIFIED_SAML_RESPONSE_57;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_ELEMENT_ASSERTION_MISSING_SAML_RESPONSE_42;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_ELEMENT_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_41;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_NAME_ID_ASSERTION_FORMAT_DIFFERENT_SAML_RESPONSE_47;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_NAME_ID_ASSERTION_FORMAT_MISSING_SAML_RESPONSE_46;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_NAME_ID_ASSERTION_FORMAT_NOT_SPECIFIED_SAML_RESPONSE_45;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_NAME_ID_ASSERTION_MISSING_SAML_RESPONSE_44;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_NAME_ID_ASSERTION_NAME_QUALIFIER_MISSING_SAML_RESPONSE_49;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_NAME_ID_ASSERTION_NAME_QUALIFIER_NOT_SPECIFIED_SAML_RESPONSE_48;
import static it.pagopa.oneid.model.Base64SAMLResponses.SUBJECT_NAME_ID_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_43;
import static it.pagopa.oneid.model.Base64SAMLResponses.UNSIGNED_ASSERTION_SAML_RESPONSE_03;
import static it.pagopa.oneid.model.Base64SAMLResponses.UNSIGNED_SAML_RESPONSE_02;
import static it.pagopa.oneid.model.Base64SAMLResponses.VERSION_ASSERTION_ATTRIBUTE_DIFFERENT_SAML_RESPONSE_35;
import static it.pagopa.oneid.model.Base64SAMLResponses.VERSION_NOT_02_SAML_RESPONSE_10;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
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
import it.pagopa.oneid.exception.SAMLResponseStatusException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.service.mock.X509CredentialTestProfile;
import it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;

@QuarkusTest
@TestProfile(X509CredentialTestProfile.class)
public class SAMLServiceImplTest {

  @ConfigProperty(name = "timestamp_spid")
  String TIMESTAMP_SPID;

  @ConfigProperty(name = "timestamp_cie")
  String TIMESTAMP_CIE;

  @ConfigProperty(name = "cie_entity_id")
  String CIE_ENTITY_ID;

  @Inject
  SAMLServiceImpl samlServiceImpl;

  @InjectSpy
  SAMLUtilsExtendedCore samlUtils;

  @InjectSpy
  MarshallerFactory marshallerFactory;

  @Inject
  SAMLUtilsConstants samlUtilsConstants;

  @InjectMock
  IDPConnectorImpl idpConnectorImpl;

  @InjectSpy
  Clock clock;

  @Test
  void buildAuthnRequest() throws OneIdentityException {
    // given
    String idpId = "dummy";
    int assertionConsumerServiceIndex = 0;
    int attributeConsumingServiceIndex = 0;
    String authLevel = "foobar";

    AuthnRequest authnRequest = samlServiceImpl.buildAuthnRequest(idpId,
        assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel);

    assertFalse(authnRequest.getID().isEmpty());
  }

  @Test
  void buildAuthnRequest_invalidAssertionConsumerServiceIndex() throws OneIdentityException {
    // given
    String idpId = "dummy";
    int assertionConsumerServiceIndex = -1;
    int attributeConsumingServiceIndex = 0;
    String authLevel = "foobar";

    assertThrows(OneIdentityException.class, () -> samlServiceImpl.buildAuthnRequest(idpId,
        assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel));
  }

  @Test
  void buildAuthnRequest_invalidAttributeConsumingServiceIndex() throws OneIdentityException {
    // given
    String idpId = "dummy";
    int assertionConsumerServiceIndex = 0;
    int attributeConsumingServiceIndex = -1;
    String authLevel = "foobar";

    assertThrows(OneIdentityException.class, () -> samlServiceImpl.buildAuthnRequest(idpId,
        assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel));
  }

  @Test
  void buildAuthnRequest_invalidIdpSSOEndpoint() throws OneIdentityException {
    // given
    String idpId = null;
    int assertionConsumerServiceIndex = 0;
    int attributeConsumingServiceIndex = 0;
    String authLevel = "foobar";

    assertThrows(OneIdentityException.class, () -> samlServiceImpl.buildAuthnRequest(idpId,
        assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel));
  }

  @Test
  void buildAuthnRequest_SAMLUtilsExceptionDuringBuildSignature() throws OneIdentityException {
    // given
    String idpId = "dummy";
    int assertionConsumerServiceIndex = 0;
    int attributeConsumingServiceIndex = 0;
    String authLevel = "foobar";

    doThrow(SAMLUtilsException.class).when(samlUtils).buildSignature(Mockito.any());
    // then

    Executable executable = () -> samlServiceImpl.buildAuthnRequest(idpId,
        assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel);
    assertThrows(GenericAuthnRequestCreationException.class, executable);
  }

  @Test
  @SneakyThrows
  void buildAuthnRequest_exceptionSigning() throws OneIdentityException {
    // given
    String idpId = "dummy";
    int assertionConsumerServiceIndex = 0;
    int attributeConsumingServiceIndex = 0;
    String authLevel = "foobar";

    Marshaller marshaller = Mockito.mock(Marshaller.class);
    marshallerFactory = Mockito.mock(MarshallerFactory.class);
    when(marshaller.marshall(Mockito.any())).thenThrow(MarshallingException.class);
    when(marshallerFactory.getMarshaller(Mockito.any(XMLObject.class))).thenReturn(marshaller);
    QuarkusMock.installMockForType(marshallerFactory, MarshallerFactory.class);

    assertThrows(GenericAuthnRequestCreationException.class,
        () -> samlServiceImpl.buildAuthnRequest(idpId,
            assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel));
  }

  @Test
  @SneakyThrows
  void buildAuthnRequest_nullMarshaller() throws OneIdentityException {
    // given
    String idpId = "dummy";
    int assertionConsumerServiceIndex = 0;
    int attributeConsumingServiceIndex = 0;
    String authLevel = "foobar";

    Marshaller marshaller = null;
    marshallerFactory = Mockito.mock(MarshallerFactory.class);
    when(marshallerFactory.getMarshaller(Mockito.any(XMLObject.class))).thenReturn(marshaller);
    QuarkusMock.installMockForType(marshallerFactory, MarshallerFactory.class);

    assertThrows(GenericAuthnRequestCreationException.class,
        () -> samlServiceImpl.buildAuthnRequest(idpId,
            assertionConsumerServiceIndex, attributeConsumingServiceIndex, authLevel));
  }

  @Test
  void checkSAMLStatus_StatusCodeSuccess() throws OneIdentityException {
    // given
    Response response = Mockito.mock(Response.class);
    Status status = Mockito.mock(Status.class);
    StatusCode statusCode = Mockito.mock(StatusCode.class);

    when(statusCode.getValue()).thenReturn(StatusCode.SUCCESS);
    when(status.getStatusCode()).thenReturn(statusCode);
    when(response.getStatus()).thenReturn(status);
    // then
    assertDoesNotThrow(() -> samlServiceImpl.checkSAMLStatus(response));
  }

  @Test
  void checkSAMLStatus_StatusCodeNull() {
    // given
    Response response = Mockito.mock(Response.class);
    Status status = Mockito.mock(Status.class);

    when(status.getStatusCode()).thenReturn(null);
    when(response.getStatus()).thenReturn(status);
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

    when(statusMessage.getValue()).thenReturn(String.valueOf(ErrorCode.ERRORCODE_NR20));
    when(statusCode.getValue()).thenReturn(StatusCode.REQUESTER);
    when(status.getStatusCode()).thenReturn(statusCode);
    when(status.getStatusMessage()).thenReturn(statusMessage);
    when(response.getStatus()).thenReturn(status);
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

    when(statusMessage.getValue()).thenReturn("");
    when(statusCode.getValue()).thenReturn(StatusCode.REQUESTER);
    when(status.getStatusCode()).thenReturn(statusCode);
    when(status.getStatusMessage()).thenReturn(statusMessage);
    when(response.getStatus()).thenReturn(status);
    // then
    assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));
  }

  @Test
  void getSAMLResponseFromString() throws OneIdentityException {
    // given
    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Response response = Mockito.mock(Response.class);
    when(samlUtils.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

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
    when(samlUtils.getAttributeDTOListFromAssertion(Mockito.any()))
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
    when(samlUtils.getAttributeDTOListFromAssertion(Mockito.any()))
        .thenReturn(Optional.empty());

    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then

    assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.getAttributesFromSAMLAssertion(assertion));
  }

  @Test
  void testGetIDPFromCieEntityId() {
    // Arrange
    IDP expectedIDP = new IDP(); // Create a mock or actual IDP object
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(CIE_ENTITY_ID, TIMESTAMP_CIE))
        .thenReturn(Optional.of(expectedIDP));

    // Act
    Optional<IDP> result = samlServiceImpl.getIDPFromEntityID(CIE_ENTITY_ID);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(expectedIDP, result.get());
    verify(idpConnectorImpl, times(1)).getIDPByEntityIDAndTimestamp(CIE_ENTITY_ID, TIMESTAMP_CIE);
    verify(idpConnectorImpl, times(0)).getIDPByEntityIDAndTimestamp(anyString(),
        eq(TIMESTAMP_SPID));
  }

  @Test
  void testGetIDPFromOtherEntityId() {
    // Arrange
    String otherEntityId = "OTHER_ENTITY_ID";
    IDP expectedIDP = new IDP(); // Create a mock or actual IDP object
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(otherEntityId, TIMESTAMP_SPID))
        .thenReturn(Optional.of(expectedIDP));

    // Act
    Optional<IDP> result = samlServiceImpl.getIDPFromEntityID(otherEntityId);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(expectedIDP, result.get());
    verify(idpConnectorImpl, times(1)).getIDPByEntityIDAndTimestamp(otherEntityId, TIMESTAMP_SPID);
    verify(idpConnectorImpl, times(0)).getIDPByEntityIDAndTimestamp(anyString(), eq(TIMESTAMP_CIE));
  }

  @Test
  void testGetIDPFromCieEntityIdReturnsEmpty() {
    // Arrange
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(CIE_ENTITY_ID, TIMESTAMP_CIE))
        .thenReturn(Optional.empty());

    // Act
    Optional<IDP> result = samlServiceImpl.getIDPFromEntityID(CIE_ENTITY_ID);

    // Assert
    assertFalse(result.isPresent());
    verify(idpConnectorImpl, times(1)).getIDPByEntityIDAndTimestamp(CIE_ENTITY_ID, TIMESTAMP_CIE);
  }

  @Test
  void testGetIDPFromOtherEntityIdReturnsEmpty() {
    // Arrange
    String otherEntityId = "OTHER_ENTITY_ID";
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(otherEntityId, TIMESTAMP_SPID))
        .thenReturn(Optional.empty());

    // Act
    Optional<IDP> result = samlServiceImpl.getIDPFromEntityID(otherEntityId);

    // Assert
    assertFalse(result.isPresent());
    verify(idpConnectorImpl, times(1)).getIDPByEntityIDAndTimestamp(otherEntityId, TIMESTAMP_SPID);
  }

  @Test
  void validateSAMLResponse_01() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(CORRECT_SAML_RESPONSE_01);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));
  }

  @Test
  void validateSAMLResponse_02() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(UNSIGNED_SAML_RESPONSE_02);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));
  }

  @Test
  void validateSAMLResponse_03() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(UNSIGNED_ASSERTION_SAML_RESPONSE_03);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));
  }

  @Test
  void validateSAMLResponse_04() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(INVALID_SIGNATURE_SAML_RESPONSE_04);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));
  }

  @Test
  void validateSAMLResponse_08() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(NOT_SPECIFIED_ID_SAML_RESPONSE_08);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Response ID not set."));
  }

  @Test
  void validateSAMLResponse_09() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(NOT_PRESENT_ID_SAML_RESPONSE_09);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Response ID not set."));
  }

  @Test
  void validateSAMLResponse_10() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(VERSION_NOT_02_SAML_RESPONSE_10);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Version different from 2.0."));
  }

  @Test
  void validateSAMLResponse_11() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUE_INSTANT_NOT_SPECIFIED_SAML_RESPONSE_11);

    Instant mockInstant = Instant.now();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Issue Instant not set."));
  }

  @Test
  void validateSAMLResponse_12() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUE_INSTANT_MISSING_SAML_RESPONSE_12);

    Instant mockInstant = Instant.now();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Issue Instant not set."));
  }

  @Test
  void validateSAMLResponse_14() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUE_INSTANT_BEFORE_REQUEST_SAML_RESPONSE_14);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.plusSeconds(10)));

    assertTrue(
        exception.getMessage().contains("Issue Instant is not after the request's Issue Instant."));
  }

  @Test
  void validateSAMLResponse_15() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUE_INSTANT_AFTER_REQUEST_SAML_RESPONSE_15);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.minusSeconds(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(
        exception.getMessage().contains("Issue Instant is not before current time."));
  }

  @Test
  void validateSAMLResponse_16() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        RESPONSE_IN_RESPONSE_TO_NOT_SPECIFIED_SAML_RESPONSE_16);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("InResponseTo not set."));
  }

  @Test
  void validateSAMLResponse_17() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        RESPONSE_IN_RESPONSE_TO_MISSING_SAML_RESPONSE_17);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("InResponseTo not set."));
  }

  @Test
  void validateSAMLResponse_19() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        DESTINATION_NOT_SPECIFIED_SAML_RESPONSE_19
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Destination not set."));
  }

  @Test
  void validateSAMLResponse_20() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        MISSING_DESTINATION_SAML_RESPONSE_20
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Destination not set."));
  }

  @Test
  void validateSAMLResponse_21() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        DESTINATION_DIFFERENT_FROM_ACS_SAML_RESPONSE_21
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Destination mismatch."));
  }

  @Test
  void validateSAMLResponse_27() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        NOT_SPECIFIED_ISSUER_ELEMENT_SAML_RESPONSE_27
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Issuer value is blank"));
  }

  @Test
  void validateSAMLResponse_28() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        MISSING_ISSUER_ELEMENT_SAML_RESPONSE_28
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Issuer not found"));
  }

  @Test
  void validateSAMLResponse_29() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUER_DIFFERENT_FROM_IDP_ENTITY_ID_SAML_RESPONSE_29
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Issuer mismatch"));
  }

  @Test
  void validateSAMLResponse_30() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        DIFFERENT_FORMAT_ATTRIBUTE_SAML_RESPONSE_30
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid format attribute for Issuer element"));
  }

  @Test
  void validateSAMLResponse_31() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        MISSING_FORMAT_ATTRIBUTE_SAML_RESPONSE_31
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid format attribute for Issuer element"));
  }

  @Test
  void validateSAMLResponse_32() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        MISSING_ASSERTION_SAML_RESPONSE_32
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Assertion not found"));
  }

  @Test
  void validateSAMLResponse_33() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        NOT_SPECIFIED_ASSERTION_ID_ATTRIBUTE_SAML_RESPONSE_33
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Assertion ID is missing"));
  }

  @Test
  void validateSAMLResponse_34() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        MISSING_ASSERTION_ID_ATTRIBUTE_SAML_RESPONSE_34
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Assertion ID is missing"));
  }

  @Test
  void validateSAMLResponse_35() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        VERSION_ASSERTION_ATTRIBUTE_DIFFERENT_SAML_RESPONSE_35
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Version different from 2.0."));
  }

  @Test
  void validateSAMLResponse_36() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUE_INSTANT_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_36);

    Instant mockInstant = Instant.now();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Issue Instant not set."));
  }

  @Test
  void validateSAMLResponse_37() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUE_INSTANT_ASSERTION_MISSING_SAML_RESPONSE_37);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.plusSeconds(10)));

    assertTrue(
        exception.getMessage().contains("Issue Instant is not after the request's Issue Instant."));
  }

  @Test
  void validateSAMLResponse_39() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUE_INSTANT_ASSERTION_BEFORE_REQUEST_INSTANT_SAML_RESPONSE_39);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.minusSeconds(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(
        exception.getMessage().contains("Issue Instant is not before current time."));
  }

  @Test
  void validateSAMLResponse_40() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUE_INSTANT_ASSERTION_AFTER_REQUEST_INSTANT_SAML_RESPONSE_40);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.minusSeconds(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(
        exception.getMessage().contains("Issue Instant is not before current time."));
  }

  @Test
  void validateSAMLResponse_41() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_ELEMENT_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_41
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Subject not correctly initialized"));
  }

  @Test
  void validateSAMLResponse_42() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_ELEMENT_ASSERTION_MISSING_SAML_RESPONSE_42
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Subject not correctly initialized"));
  }

  @Test
  void validateSAMLResponse_43() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_NAME_ID_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_43
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid NameQualifier for Subject"));
  }

  @Test
  void validateSAMLResponse_44() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_NAME_ID_ASSERTION_MISSING_SAML_RESPONSE_44
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Subject not correctly initialized"));
  }

  @Test
  void validateSAMLResponse_45() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_NAME_ID_ASSERTION_FORMAT_NOT_SPECIFIED_SAML_RESPONSE_45
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid Name ID type for Subject"));
  }

  @Test
  void validateSAMLResponse_46() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_NAME_ID_ASSERTION_FORMAT_MISSING_SAML_RESPONSE_46
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid Name ID type for Subject"));
  }

  @Test
  void validateSAMLResponse_47() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_NAME_ID_ASSERTION_FORMAT_DIFFERENT_SAML_RESPONSE_47
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid NameId format for Subject"));
  }

  @Test
  void validateSAMLResponse_48() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_NAME_ID_ASSERTION_NAME_QUALIFIER_NOT_SPECIFIED_SAML_RESPONSE_48
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid NameQualifier for Subject"));
  }

  @Test
  void validateSAMLResponse_49() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_NAME_ID_ASSERTION_NAME_QUALIFIER_MISSING_SAML_RESPONSE_49
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid NameQualifier for Subject"));
  }

  @Test
  void validateSAMLResponse_51() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_51
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("SubjectConfirmationData not found"));
  }

  @Test
  void validateSAMLResponse_52() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_ASSERTION_MISSING_SAML_RESPONSE_52);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("SubjectConfirmation not correctly initialized"));
  }

  @Test
  void validateSAMLResponse_53() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_ASSERTION_METHOD_NOT_SPECIFIED_SAML_RESPONSE_53);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid method attribute for SubjectConfirmation"));
  }

  @Test
  void validateSAMLResponse_54() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_ASSERTION_METHOD_MISSING_SAML_RESPONSE_54
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid method attribute for SubjectConfirmation"));
  }

  @Test
  void validateSAMLResponse_55() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_ASSERTION_METHOD_DIFFERENT_SAML_RESPONSE_55
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Invalid method attribute for SubjectConfirmation"));
  }

  @Test
  void validateSAMLResponse_56() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_DATA_MISSING_SAML_RESPONSE_56
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("SubjectConfirmationData not found"));
  }

  @Test
  void validateSAMLResponse_57() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_DATA_RECIPIENT_NOT_SPECIFIED_SAML_RESPONSE_57
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Recipient not found"));
  }

  @Test
  void validateSAMLResponse_58() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_DATA_RECIPIENT_MISSING_SAML_RESPONSE_58
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Recipient not found"));
  }

  @Test
  void validateSAMLResponse_59() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_DATA_RECIPIENT_DIFFERENT_SAML_RESPONSE_59
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("Recipient mismatch"));
  }

  @Test
  void validateSAMLResponse_60() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_DATA_IN_RESPONSE_TO_NOT_SPECIFIED_SAML_RESPONSE_60
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("InResponseTo not set."));
  }

  @Test
  void validateSAMLResponse_61() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_DATA_IN_RESPONSE_TO_MISSING_SAML_RESPONSE_61
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("InResponseTo not set."));
  }

  @Test
  void validateSAMLResponse_63() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_DATA_NOT_ON_OR_AFTER_NOT_DEFINED_SAML_RESPONSE_63
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("NotOnOrAfter not found"));
  }

  @Test
  void validateSAMLResponse_64() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_DATA_NOT_ON_OR_AFTER_MISSING_SAML_RESPONSE_64
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("NotOnOrAfter not found"));
  }

  @Test
  void validateSAMLResponse_66() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SUBJECT_CONFIRMATION_DATA_NOT_ON_OR_AFTER_BEFORE_RESPONSE_INSTANT_SAML_RESPONSE_66
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doThrow(SAMLValidationException.class).when(samlUtils)
        .validateSignature(Mockito.any(), Mockito.any());
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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10)));

    assertTrue(exception.getMessage().contains("NotOnOrAfter expired"));
  }


  /*
  @Test
  void validateSAMLResponse_invalidSignature() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    when(subjectConfirmationData.getRecipient()).thenReturn(SAMLUtilsConstants.ACS_URL);
    when(
        subjectConfirmationData.getNotOnOrAfter()).thenReturn(Instant.now().plusSeconds(60));

    when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    when(assertion.getSubject()).thenReturn(subject);

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
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));

    // then
    assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID, Set.of("fiscalNumber")));
  }

  @Test
  void validateSAMLResponse_noAssertion() {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    List<Assertion> assertions = new ArrayList<>();
    when(response.getAssertions()).thenReturn(assertions);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID, Set.of("fiscalNumber")));
  }

  @Test
  void validateSAMLResponse_nullSubjectConfirmationData() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);

    when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(null);
    when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID, Set.of("fiscalNumber")));
  }

  @Test
  void validateSAMLResponse_differentRecipient() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    when(subjectConfirmationData.getRecipient()).thenReturn("dummy");

    when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID, Set.of("fiscalNumber")));
  }

  @Test
  void validateSAMLResponse_nullRecipient() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    when(subjectConfirmationData.getRecipient()).thenReturn(null);

    when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID, Set.of("fiscalNumber")));
  }

  @Test
  void validateSAMLResponse_nullGetNotOnOrAfter() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    when(subjectConfirmationData.getRecipient()).thenReturn(SAMLUtilsConstants.ACS_URL);
    when(
        subjectConfirmationData.getNotOnOrAfter()).thenReturn(null);

    when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID, Set.of("fiscalNumber")));
  }

  @Test
  void validateSAMLResponse_notValidGetNotOnOrAfter() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    when(subjectConfirmationData.getRecipient()).thenReturn(SAMLUtilsConstants.ACS_URL);
    when(
        subjectConfirmationData.getNotOnOrAfter()).thenReturn(Instant.now().minusSeconds(60));

    when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);

    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID, Set.of("fiscalNumber")));
  }

  @Test
  void validateSAMLResponse_notFoundIDP() throws SAMLUtilsException {
    // given
    String entityID = "dummy";
    Response response = Mockito.mock(Response.class);
    Assertion assertion = Mockito.mock(Assertion.class);
    when(response.getAssertions()).thenReturn(List.of(assertion));

    Subject subject = Mockito.mock(Subject.class);
    SubjectConfirmation subjectConfirmation = Mockito.mock(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = Mockito.mock(SubjectConfirmationData.class);

    when(subjectConfirmationData.getRecipient()).thenReturn(SAMLUtilsConstants.ACS_URL);
    when(
        subjectConfirmationData.getNotOnOrAfter()).thenReturn(Instant.now().plusSeconds(60));

    when(subjectConfirmation.getSubjectConfirmationData())
        .thenReturn(subjectConfirmationData);
    when(subject.getSubjectConfirmations()).thenReturn(List.of(subjectConfirmation));
    when(assertion.getSubject()).thenReturn(subject);

    samlUtils = Mockito.mock(SAMLUtilsExtendedCore.class);
    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    QuarkusMock.installMockForType(samlUtils, SAMLUtilsExtendedCore.class);
    // then
    assertThrows(SAMLValidationException.class,
        () -> samlServiceImpl.validateSAMLResponse(response, entityID, Set.of("fiscalNumber")));
  }


   */


}
