package it.pagopa.oneid.service;

import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.ERRORCODE_NR19;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.ERRORCODE_NR20;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.ERRORCODE_NR21;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.ERRORCODE_NR22;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.ERRORCODE_NR23;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.ERRORCODE_NR25;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ASSERTION_ID_MISSING;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ASSERTION_NOT_FOUND;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_MATCHING;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_MATCHING_FOR_CIE;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_OBTAINED_FROM_SAML_ASSERTION;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_PRESENT;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ATTRIBUTES_STATEMENTS_NOT_PRESENT;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_AUDIENCE_MISMATCH;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_AUDIENCE_RESTRICTIONS_MISSING_OR_EMPTY;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_MISSING;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_NOT_MATCHING_REQUESTED_AUTH_LEVEL;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_AUTHN_CONTEXT_MISSING;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_AUTHN_STATEMENTS_MISSING_OR_EMPTY;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_INVALID_AUTHN_CONTEXT_CLASS_REF;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_INVALID_NAME_ID_FORMAT;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_INVALID_NAME_ID_TYPE;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_INVALID_NAME_QUALIFIER;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_IN_RESPONSE_TO_MISSING;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ISSUER_INVALID_FORMAT;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ISSUER_MISMATCH;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ISSUER_NOT_FOUND;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_ISSUER_VALUE_BLANK;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_MISSING_CONDITIONS;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_NOT_BEFORE_IN_THE_FUTURE;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_NOT_BEFORE_NOT_FOUND;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_NOT_ON_OR_AFTER_EXPIRED;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_NOT_ON_OR_AFTER_NOT_FOUND;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_RECIPIENT_MISMATCH;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_SUBJECT_CONFIRMATION_DATA_NOT_FOUND;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_SUBJECT_CONFIRMATION_INVALID_METHOD_ATTRIBUTE;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_SUBJECT_CONFIRMATION_NOT_INITIALIZED;
import static it.pagopa.oneid.common.model.exception.enums.ErrorCode.IDP_ERROR_SUBJECT_NOT_INITIALIZED;
import static it.pagopa.oneid.model.Base64SAMLResponses.ASSERTION_MISSING_AUDIENCE_RESTRICTION_CONDITION_SAML_RESPONSE_84;
import static it.pagopa.oneid.model.Base64SAMLResponses.ASSERTION_UNSPECIFIED_AUDIENCE_RESTRICTION_CONDITION_SAML_RESPONSE_83;
import static it.pagopa.oneid.model.Base64SAMLResponses.ATTRIBUTES_WITHOUT_NAME_FORMAT_SAML_RESPONSE_109;
import static it.pagopa.oneid.model.Base64SAMLResponses.ATTRIBUTE_STATEMENT_PRESENT_BUT_WITH_SUBELEMENT_ATTRIBUTE_MISSING_SAML_RESPONSE_98;
import static it.pagopa.oneid.model.Base64SAMLResponses.ATTRIBUTE_STATEMENT_PRESENT_BUT_WITH_SUBELEMENT_ATTRIBUTE_UNSPECIFIED_SAML_RESPONSE_99;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUDIENCE_DIFFERENT_THAN_ENTITY_ID_OF_SP_SAML_RESPONSE_87;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUDIENCE_MISSING_SAML_RESPONSE_86;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUDIENCE_UNSPECIFIED_SAML_RESPONSE_85;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUTH_CONTEXT_CLASS_REF_MISSING_SAML_RESPONSE_93;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUTH_CONTEXT_CLASS_REF_UNSPECIFIED_SAML_RESPONSE_92;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUTH_CONTEXT_CLASS_REF_WITH_L1_VALUE_SAML_RESPONSE_94;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUTH_CONTEXT_CLASS_REF_WITH_L2_VALUE_SAML_RESPONSE_95;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUTH_CONTEXT_CLASS_REF_WITH_L3_VALUE_SAML_RESPONSE_96;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUTH_CONTEXT_CLASS_REF_WITH_UNEXPECTED_VALUE_SAML_RESPONSE_97;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUTH_CONTEXT_MISSING_SAML_RESPONSE_SAML_RESPONSE_91;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUTH_CONTEXT_UNSPECIFIED_SAML_RESPONSE_SAML_RESPONSE_90;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUTH_STATEMENT_MISSING_SAML_RESPONSE_89;
import static it.pagopa.oneid.model.Base64SAMLResponses.AUTH_STATEMENT_UNSPECIFIED_SAML_RESPONSE_88;
import static it.pagopa.oneid.model.Base64SAMLResponses.BLOCKED_CREDENTIALS_SAML_RESPONSE_108;
import static it.pagopa.oneid.model.Base64SAMLResponses.CIE_EMPTY_ATTRIBUTE_STATEMENT_SAMLRESPONSE;
import static it.pagopa.oneid.model.Base64SAMLResponses.CIE_EXTRA_EIDAS_ATTRIBUTE_SAMLRESPONSE;
import static it.pagopa.oneid.model.Base64SAMLResponses.CIE_EXTRA_INVALID_ATTRIBUTE_SAMLRESPONSE;
import static it.pagopa.oneid.model.Base64SAMLResponses.CIE_EXTRA_NOT_EIDAS_ATTRIBUTE_SAMLRESPONSE;
import static it.pagopa.oneid.model.Base64SAMLResponses.CIE_MISSING_ATTRIBUTE_STATEMENT_SAMLRESPONSE;
import static it.pagopa.oneid.model.Base64SAMLResponses.CIE_VALID_SAMLRESPONSE;
import static it.pagopa.oneid.model.Base64SAMLResponses.CONDITIONS_ELEMENT_MISSING_SAML_RESPONSE_74;
import static it.pagopa.oneid.model.Base64SAMLResponses.CONDITIONS_ELEMENT_NOT_BEFORE_AFTER_RESPONSE_SAML_RESPONSE_78;
import static it.pagopa.oneid.model.Base64SAMLResponses.CONDITIONS_ELEMENT_NOT_BEFORE_MISSING_SAML_RESPONSE_76;
import static it.pagopa.oneid.model.Base64SAMLResponses.CONDITIONS_ELEMENT_NOT_BEFORE_NOT_SPECIFIED_SAML_RESPONSE_75;
import static it.pagopa.oneid.model.Base64SAMLResponses.CONDITIONS_ELEMENT_NOT_ON_OR_AFTER_NOT_SPECIFIED_SAML_RESPONSE_79;
import static it.pagopa.oneid.model.Base64SAMLResponses.CONDITIONS_ELEMENT_NOT_SPECIFIED_SAML_RESPONSE_73;
import static it.pagopa.oneid.model.Base64SAMLResponses.CONSENT_NEGATED_SAML_RESPONSE_107;
import static it.pagopa.oneid.model.Base64SAMLResponses.DESTINATION_DIFFERENT_FROM_ACS_SAML_RESPONSE_21;
import static it.pagopa.oneid.model.Base64SAMLResponses.DESTINATION_NOT_SPECIFIED_SAML_RESPONSE_19;
import static it.pagopa.oneid.model.Base64SAMLResponses.DIFFERENT_FORMAT_ATTRIBUTE_SAML_RESPONSE_30;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUER_ASSERTION_DIFFERENT_SAML_RESPONSE_69;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUER_ASSERTION_FORMAT_ATTRIBUTE_DIFFERENT_SAML_RESPONSE_72;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUER_ASSERTION_FORMAT_ATTRIBUTE_MISSING_SAML_RESPONSE_71;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUER_ASSERTION_FORMAT_ATTRIBUTE_NOT_SPECIFIED_SAML_RESPONSE_70;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUER_ASSERTION_MISSING_SAML_RESPONSE_68;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUER_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_67;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUER_DIFFERENT_FROM_IDP_ENTITY_ID_SAML_RESPONSE_29;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_AFTER_REQUEST_SAML_RESPONSE_15;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_ASSERTION_AFTER_REQUEST_INSTANT_SAML_RESPONSE_40;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_ASSERTION_BEFORE_REQUEST_INSTANT_SAML_RESPONSE_39;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_ASSERTION_MISSING_SAML_RESPONSE_37;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_36;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_BEFORE_REQUEST_SAML_RESPONSE_14;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_MISSING_SAML_RESPONSE_12;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_NOT_SPECIFIED_SAML_RESPONSE_11;
import static it.pagopa.oneid.model.Base64SAMLResponses.ISSUE_INSTANT_WITH_MILLISECONDS_SAML_RESPONSE_110;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_ASSERTION_ID_ATTRIBUTE_SAML_RESPONSE_34;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_ASSERTION_SAML_RESPONSE_32;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_DESTINATION_SAML_RESPONSE_20;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_ISSUER_ELEMENT_SAML_RESPONSE_28;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_STATUS_CODE_SAML_RESPONSE_25;
import static it.pagopa.oneid.model.Base64SAMLResponses.MISSING_STATUS_SAML_RESPONSE_23;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_ON_OR_AFTER_BEFORE_RESPONSE_RECEPTION_SAML_RESPONSE_82;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_ON_OR_AFTER_MISSING_SAML_RESPONSE_80;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_PRESENT_ID_SAML_RESPONSE_09;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_SPECIFIED_ASSERTION_ID_ATTRIBUTE_SAML_RESPONSE_33;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_SPECIFIED_ID_SAML_RESPONSE_08;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_SPECIFIED_ISSUER_ELEMENT_SAML_RESPONSE_27;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_SPECIFIED_STATUS_CODE_SAML_RESPONSE_24;
import static it.pagopa.oneid.model.Base64SAMLResponses.NOT_SPECIFIED_STATUS_SAML_RESPONSE_22;
import static it.pagopa.oneid.model.Base64SAMLResponses.PROCESS_CANCELED_BY_USER_SAML_RESPONSE_111;
import static it.pagopa.oneid.model.Base64SAMLResponses.REPEATED_WRONG_CREDENTIALS_SUBMITTED_SAML_RESPONSE_104;
import static it.pagopa.oneid.model.Base64SAMLResponses.RESPONSE_IN_RESPONSE_TO_MISSING_SAML_RESPONSE_17;
import static it.pagopa.oneid.model.Base64SAMLResponses.RESPONSE_IN_RESPONSE_TO_NOT_SPECIFIED_SAML_RESPONSE_16;
import static it.pagopa.oneid.model.Base64SAMLResponses.SENT_ATTRIBUTE_SET_DIFFERENT_THAN_REQUESTED_SAML_RESPONSE_103;
import static it.pagopa.oneid.model.Base64SAMLResponses.STATUS_CODE_DIFFERENT_FROM_SUCCESS_SAML_RESPONSE_26;
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
import static it.pagopa.oneid.model.Base64SAMLResponses.TIMEOUT_SAML_RESPONSE_106;
import static it.pagopa.oneid.model.Base64SAMLResponses.USER_WITHOUT_COMPATIBLE_CREDENTIALS_SAML_RESPONSE_105;
import static it.pagopa.oneid.model.Base64SAMLResponses.VERSION_ASSERTION_ATTRIBUTE_DIFFERENT_SAML_RESPONSE_35;
import static it.pagopa.oneid.model.Base64SAMLResponses.VERSION_NOT_02_SAML_RESPONSE_10;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.IDPStatus;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
@TestProfile(X509CredentialTestProfile.class)
public class SAMLServiceImplTest {

  private static final Logger log = LoggerFactory.getLogger(SAMLServiceImplTest.class);
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

  @InjectMock
  IDPConnectorImpl idpConnectorImpl;

  @InjectSpy
  Clock clock;

  @BeforeEach
  void beforeEach() {
    doNothing().when(samlUtils).marshallAndSign(Mockito.any(), Mockito.any());
  }


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

    when(statusMessage.getValue()).thenReturn(String.valueOf(ERRORCODE_NR20));
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
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_22() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(NOT_SPECIFIED_STATUS_SAML_RESPONSE_22);
    // then
    Exception exception = assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains("Status Code not set."));
  }

  @Test
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_23() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(MISSING_STATUS_SAML_RESPONSE_23);
    // then
    Exception exception = assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains("Status message not set."));
  }

  @Test
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_24() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        NOT_SPECIFIED_STATUS_CODE_SAML_RESPONSE_24);
    // then
    Exception exception = assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains("Status Code not set."));
  }

  @Test
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_25() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        MISSING_STATUS_CODE_SAML_RESPONSE_25);
    // then
    Exception exception = assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains("Status Code not set."));
  }

  @Test
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_26() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        STATUS_CODE_DIFFERENT_FROM_SUCCESS_SAML_RESPONSE_26);
    // then
    Exception exception = assertThrows(OneIdentityException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains("Status message not set."));
  }

  @Test
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_104() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        REPEATED_WRONG_CREDENTIALS_SUBMITTED_SAML_RESPONSE_104);
    // then
    Exception exception = assertThrows(SAMLResponseStatusException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains(ERRORCODE_NR19.getErrorCode()));
  }

  @Test
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_105() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        USER_WITHOUT_COMPATIBLE_CREDENTIALS_SAML_RESPONSE_105);
    // then
    Exception exception = assertThrows(SAMLResponseStatusException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains(ERRORCODE_NR20.getErrorCode()));
  }

  @Test
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_106() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        TIMEOUT_SAML_RESPONSE_106);
    // then
    Exception exception = assertThrows(SAMLResponseStatusException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains(ERRORCODE_NR21.getErrorCode()));
  }

  @Test
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_107() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CONSENT_NEGATED_SAML_RESPONSE_107);
    // then
    Exception exception = assertThrows(SAMLResponseStatusException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains(ERRORCODE_NR22.getErrorCode()));
  }

  @Test
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_108() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        BLOCKED_CREDENTIALS_SAML_RESPONSE_108);
    // then
    Exception exception = assertThrows(SAMLResponseStatusException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains(ERRORCODE_NR23.getErrorCode()));
  }

  @Test
  @SneakyThrows
  void checkSAMLStatus_SAMLResponse_111() {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        PROCESS_CANCELED_BY_USER_SAML_RESPONSE_111
    );
    // then
    Exception exception = assertThrows(SAMLResponseStatusException.class,
        () -> samlServiceImpl.checkSAMLStatus(response));

    assertTrue(exception.getMessage().contains(ERRORCODE_NR25.getErrorCode()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_NOT_VALID_RESPONSE_ID.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_NOT_VALID_RESPONSE_ID.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_INVALID_SAML_VERSION.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_ISSUE_INSTANT_MISSING.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_ISSUE_INSTANT_MISSING.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.plusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(ErrorCode.IDP_ERROR_ISSUE_INSTANT_AFTER_REQUEST.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(ErrorCode.IDP_ERROR_ISSUE_INSTANT_IN_THE_FUTURE.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_IN_RESPONSE_TO_MISSING.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_IN_RESPONSE_TO_MISSING.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_DESTINATION_NOT_FOUND.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_DESTINATION_NOT_FOUND.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_DESTINATION_MISMATCH.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(ErrorCode.IDP_ERROR_ISSUER_VALUE_BLANK.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(ErrorCode.IDP_ERROR_ISSUER_NOT_FOUND.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(ErrorCode.IDP_ERROR_ISSUER_MISMATCH.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_ISSUER_INVALID_FORMAT.getErrorMessage()));
  }

  /* TODO: fix this test
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
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

  }
   */

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_ASSERTION_NOT_FOUND.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_ASSERTION_ID_MISSING.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_ASSERTION_ID_MISSING.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_INVALID_SAML_VERSION.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_36() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUE_INSTANT_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_36);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_ISSUE_INSTANT_MISSING.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.plusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(ErrorCode.IDP_ERROR_ISSUE_INSTANT_AFTER_REQUEST.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(ErrorCode.IDP_ERROR_ISSUE_INSTANT_IN_THE_FUTURE.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(ErrorCode.IDP_ERROR_ISSUE_INSTANT_IN_THE_FUTURE.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_SUBJECT_NOT_INITIALIZED.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(ErrorCode.IDP_ERROR_SUBJECT_NOT_INITIALIZED.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_INVALID_NAME_QUALIFIER.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_SUBJECT_NOT_INITIALIZED.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_INVALID_NAME_ID_TYPE.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_INVALID_NAME_ID_TYPE.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_INVALID_NAME_ID_FORMAT.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_INVALID_NAME_QUALIFIER.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_INVALID_NAME_QUALIFIER.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(IDP_ERROR_SUBJECT_CONFIRMATION_DATA_NOT_FOUND.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(IDP_ERROR_SUBJECT_CONFIRMATION_NOT_INITIALIZED.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(IDP_ERROR_SUBJECT_CONFIRMATION_INVALID_METHOD_ATTRIBUTE.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(IDP_ERROR_SUBJECT_CONFIRMATION_INVALID_METHOD_ATTRIBUTE.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(IDP_ERROR_SUBJECT_CONFIRMATION_INVALID_METHOD_ATTRIBUTE.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage()
        .contains(IDP_ERROR_SUBJECT_CONFIRMATION_DATA_NOT_FOUND.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(ErrorCode.IDP_ERROR_RECIPIENT_NOT_FOUND.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(ErrorCode.IDP_ERROR_RECIPIENT_NOT_FOUND.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_RECIPIENT_MISMATCH.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_IN_RESPONSE_TO_MISSING.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_IN_RESPONSE_TO_MISSING.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_NOT_ON_OR_AFTER_NOT_FOUND.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_NOT_ON_OR_AFTER_NOT_FOUND.getErrorMessage()));
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_NOT_ON_OR_AFTER_EXPIRED.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_67() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUER_ASSERTION_NOT_SPECIFIED_SAML_RESPONSE_67
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_ISSUER_VALUE_BLANK.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_68() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUER_ASSERTION_MISSING_SAML_RESPONSE_68
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_ISSUER_NOT_FOUND.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_69() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUER_ASSERTION_DIFFERENT_SAML_RESPONSE_69
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_ISSUER_MISMATCH.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_70() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUER_ASSERTION_FORMAT_ATTRIBUTE_NOT_SPECIFIED_SAML_RESPONSE_70
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_ISSUER_INVALID_FORMAT.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_71() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUER_ASSERTION_FORMAT_ATTRIBUTE_MISSING_SAML_RESPONSE_71
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_ISSUER_INVALID_FORMAT.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_72() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUER_ASSERTION_FORMAT_ATTRIBUTE_DIFFERENT_SAML_RESPONSE_72
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(exception.getMessage().contains(IDP_ERROR_ISSUER_INVALID_FORMAT.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_73() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CONDITIONS_ELEMENT_NOT_SPECIFIED_SAML_RESPONSE_73
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_AUDIENCE_RESTRICTIONS_MISSING_OR_EMPTY.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_74() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CONDITIONS_ELEMENT_MISSING_SAML_RESPONSE_74
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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_MISSING_CONDITIONS.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_75() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CONDITIONS_ELEMENT_NOT_BEFORE_NOT_SPECIFIED_SAML_RESPONSE_75);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_NOT_BEFORE_NOT_FOUND.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_76() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CONDITIONS_ELEMENT_NOT_BEFORE_MISSING_SAML_RESPONSE_76);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_NOT_BEFORE_NOT_FOUND.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_78() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CONDITIONS_ELEMENT_NOT_BEFORE_AFTER_RESPONSE_SAML_RESPONSE_78);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_NOT_BEFORE_IN_THE_FUTURE.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_79() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CONDITIONS_ELEMENT_NOT_ON_OR_AFTER_NOT_SPECIFIED_SAML_RESPONSE_79);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_NOT_ON_OR_AFTER_NOT_FOUND.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_80() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        NOT_ON_OR_AFTER_MISSING_SAML_RESPONSE_80);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_NOT_ON_OR_AFTER_NOT_FOUND.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_82() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        NOT_ON_OR_AFTER_BEFORE_RESPONSE_RECEPTION_SAML_RESPONSE_82);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_NOT_ON_OR_AFTER_EXPIRED.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_83() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ASSERTION_UNSPECIFIED_AUDIENCE_RESTRICTION_CONDITION_SAML_RESPONSE_83);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_AUDIENCE_RESTRICTIONS_MISSING_OR_EMPTY.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_84() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ASSERTION_MISSING_AUDIENCE_RESTRICTION_CONDITION_SAML_RESPONSE_84);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_AUDIENCE_RESTRICTIONS_MISSING_OR_EMPTY.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_85() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUDIENCE_UNSPECIFIED_SAML_RESPONSE_85);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_AUDIENCE_MISMATCH.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_86() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUDIENCE_MISSING_SAML_RESPONSE_86);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_AUDIENCE_RESTRICTIONS_MISSING_OR_EMPTY.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_87() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUDIENCE_DIFFERENT_THAN_ENTITY_ID_OF_SP_SAML_RESPONSE_87);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_AUDIENCE_MISMATCH.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_88() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_STATEMENT_UNSPECIFIED_SAML_RESPONSE_88);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_AUTHN_CONTEXT_MISSING.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_89() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_STATEMENT_MISSING_SAML_RESPONSE_89);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_AUTHN_STATEMENTS_MISSING_OR_EMPTY.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_90() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_UNSPECIFIED_SAML_RESPONSE_SAML_RESPONSE_90);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_MISSING.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_91() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_MISSING_SAML_RESPONSE_SAML_RESPONSE_91);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_AUTHN_CONTEXT_MISSING.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_92() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_UNSPECIFIED_SAML_RESPONSE_92);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_MISSING.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_93() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_MISSING_SAML_RESPONSE_93);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_MISSING.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_94_L1_ok_requestedL1() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_L1_VALUE_SAML_RESPONSE_94);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
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
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "spidCode"), mockInstant.minusSeconds(10), AuthLevel.L1));
  }

  @Test
  void validateSAMLResponse_94_L1_ko_requestedL2() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_L1_VALUE_SAML_RESPONSE_94);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(
                IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_NOT_MATCHING_REQUESTED_AUTH_LEVEL.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_94_L1_ko_requestedL3() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_L1_VALUE_SAML_RESPONSE_94);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L3));

    assertTrue(
        exception.getMessage()
            .contains(
                IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_NOT_MATCHING_REQUESTED_AUTH_LEVEL.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_95_L2_ok_requestedL1() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_L2_VALUE_SAML_RESPONSE_95);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
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
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "spidCode"), mockInstant.minusSeconds(10), AuthLevel.L1));
  }

  @Test
  void validateSAMLResponse_95_L2_ok_requestedL2() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_L2_VALUE_SAML_RESPONSE_95);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
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
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "spidCode"), mockInstant.minusSeconds(10), AuthLevel.L2));
  }

  @Test
  void validateSAMLResponse_95_L2_ko_requestedL3() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_L2_VALUE_SAML_RESPONSE_95);

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
    SAMLValidationException exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L3));

    assertTrue(
        exception.getErrorCode().getErrorMessage()
            .contains(
                IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_NOT_MATCHING_REQUESTED_AUTH_LEVEL.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_96_L3_ok_requestedL1() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_L3_VALUE_SAML_RESPONSE_96);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
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
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "spidCode"), mockInstant.minusSeconds(10), AuthLevel.L1));
  }

  @Test
  void validateSAMLResponse_96_L3_ok_requestedL2() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_L3_VALUE_SAML_RESPONSE_96);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
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
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "spidCode"), mockInstant.minusSeconds(10), AuthLevel.L2));
  }

  @Test
  void validateSAMLResponse_96_L3_ok_requestedL3() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_L3_VALUE_SAML_RESPONSE_96);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
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
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "spidCode"), mockInstant.minusSeconds(10), AuthLevel.L3));
  }

  @Test
  void validateSAMLResponse_96() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_L3_VALUE_SAML_RESPONSE_96);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
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
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "spidCode"), mockInstant.minusSeconds(10), AuthLevel.L2));
  }

  @Test
  void validateSAMLResponse_97() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        AUTH_CONTEXT_CLASS_REF_WITH_UNEXPECTED_VALUE_SAML_RESPONSE_97);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_INVALID_AUTHN_CONTEXT_CLASS_REF.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_98() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ATTRIBUTE_STATEMENT_PRESENT_BUT_WITH_SUBELEMENT_ATTRIBUTE_MISSING_SAML_RESPONSE_98);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_ATTRIBUTES_NOT_PRESENT.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_99() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ATTRIBUTE_STATEMENT_PRESENT_BUT_WITH_SUBELEMENT_ATTRIBUTE_UNSPECIFIED_SAML_RESPONSE_99);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_ATTRIBUTES_NOT_OBTAINED_FROM_SAML_ASSERTION.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_103() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        SENT_ATTRIBUTE_SET_DIFFERENT_THAN_REQUESTED_SAML_RESPONSE_103);

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
                Set.of("fiscalNumber", "dateOfBirth"), mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage().contains(IDP_ERROR_ATTRIBUTES_NOT_MATCHING.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_109() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ATTRIBUTES_WITHOUT_NAME_FORMAT_SAML_RESPONSE_109);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
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
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "spidCode"), mockInstant.minusSeconds(10), AuthLevel.L2));

  }

  @Test
  void validateSAMLResponse_110() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        ISSUE_INSTANT_WITH_MILLISECONDS_SAML_RESPONSE_110);

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
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
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("fiscalNumber", "spidCode"), mockInstant.minusSeconds(10), AuthLevel.L2));
  }

  //CIE

  @Test
  void validateSAMLResponse_CIE_VALID_SAMLRESPONSE() throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CIE_VALID_SAMLRESPONSE
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    IDP testIDP = IDP.builder()
        .entityID(
            "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_CIE))
        .status(IDPStatus.OK)
        .build();
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    assertDoesNotThrow(
        () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
            Set.of("spidCode", "fiscalNumber"), // requested
            mockInstant.minusSeconds(10), AuthLevel.L2));
  }

  @Test
  void validateSAMLResponse_CIE_EXTRA_EIDAS_ATTRIBUTE_SAMLRESPONSE()
      throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CIE_EXTRA_EIDAS_ATTRIBUTE_SAMLRESPONSE
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    IDP testIDP = IDP.builder()
        .entityID(
            "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_CIE))
        .status(IDPStatus.OK)
        .build();
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "familyName", "dateOfBirth"), // requested
                mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_ATTRIBUTES_NOT_MATCHING_FOR_CIE.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_CIE_EXTRA_NOT_EIDAS_ATTRIBUTE_SAMLRESPONSE()
      throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CIE_EXTRA_NOT_EIDAS_ATTRIBUTE_SAMLRESPONSE
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    IDP testIDP = IDP.builder()
        .entityID(
            "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_CIE))
        .status(IDPStatus.OK)
        .build();
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "familyName", "dateOfBirth"), // requested
                mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_ATTRIBUTES_NOT_MATCHING_FOR_CIE.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_CIE_EXTRA_INVALID_ATTRIBUTE_SAMLRESPONSE()
      throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CIE_EXTRA_INVALID_ATTRIBUTE_SAMLRESPONSE
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    IDP testIDP = IDP.builder()
        .entityID(
            "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_CIE))
        .status(IDPStatus.OK)
        .build();
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "familyName", "dateOfBirth"),
                // requested
                mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_ATTRIBUTES_NOT_MATCHING_FOR_CIE.getErrorMessage()));
  }


  @Test
  void validateSAMLResponse_CIE_EMPTY_ATTRIBUTE_STATEMENT_SAMLRESPONSE()
      throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CIE_EMPTY_ATTRIBUTE_STATEMENT_SAMLRESPONSE
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    IDP testIDP = IDP.builder()
        .entityID(
            "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_CIE))
        .status(IDPStatus.OK)
        .build();
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "familyName", "dateOfBirth"),
                // requested
                mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_ATTRIBUTES_NOT_PRESENT.getErrorMessage()));
  }

  @Test
  void validateSAMLResponse_CIE_MISSING_ATTRIBUTE_STATEMENT_SAMLRESPONSE()
      throws OneIdentityException {
    // given
    Response response = samlUtils.getSAMLResponseFromString(
        CIE_MISSING_ATTRIBUTE_STATEMENT_SAMLRESPONSE
    );

    Instant mockInstant = response.getIssueInstant();

    Mockito.doNothing().when(samlUtils).validateSignature(Mockito.any(), Mockito.any());
    IDP testIDP = IDP.builder()
        .entityID(
            "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_CIE))
        .status(IDPStatus.OK)
        .build();
    when(idpConnectorImpl.getIDPByEntityIDAndTimestamp(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    when(clock.instant()).thenReturn(mockInstant.plusMillis(10));

    // then
    Exception exception =
        assertThrows(SAMLValidationException.class,
            () -> samlServiceImpl.validateSAMLResponse(response, testIDP.getEntityID(),
                Set.of("fiscalNumber", "familyName", "dateOfBirth"),
                // requested
                mockInstant.minusSeconds(10), AuthLevel.L2));

    assertTrue(
        exception.getMessage()
            .contains(IDP_ERROR_ATTRIBUTES_STATEMENTS_NOT_PRESENT.getErrorMessage()));
  }
}