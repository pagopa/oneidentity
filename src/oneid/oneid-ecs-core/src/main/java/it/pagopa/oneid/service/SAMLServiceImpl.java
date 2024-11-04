package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.SAMLResponseStatusException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.xmlsec.signature.Signature;
import org.w3c.dom.Element;

@ApplicationScoped
@CustomLogging
public class SAMLServiceImpl implements SAMLService {

  private final static Set<String> eidasMinimumDataSet = Set.of(
      //Mandatory attributes
      Identifier.name.name(),
      Identifier.familyName.name(),
      Identifier.dateOfBirth.name(),
      Identifier.fiscalNumber.name(),
      //Optional attributes
      Identifier.placeOfBirth.name(),
      Identifier.address.name(),
      Identifier.gender.name()
  );

  private final Clock clock;

  @Inject
  SAMLUtilsExtendedCore samlUtils;

  @Inject
  IDPConnectorImpl idpConnectorImpl;

  @ConfigProperty(name = "timestamp_spid")
  String TIMESTAMP_SPID;

  @ConfigProperty(name = "timestamp_cie")
  String TIMESTAMP_CIE;

  @ConfigProperty(name = "cie_entity_id")
  String CIE_ENTITY_ID;

  @ConfigProperty(name = "base_path")
  String BASE_PATH;

  @ConfigProperty(name = "entity_id")
  String ENTITY_ID;

  @ConfigProperty(name = "acs_url")
  String ACS_URL;


  @Inject
  SAMLServiceImpl(Clock clock) {
    this.clock = clock;
  }

  private static void validateInResponseTo(String inResponseTo) {
    if (inResponseTo == null || inResponseTo.isBlank()) {
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_IN_RESPONSE_TO_MISSING);
    }
  }

  private static void validateSAMLVersion(SAMLVersion samlVersion) {
    if (samlVersion == null || !samlVersion.equals(SAMLVersion.VERSION_20)) {
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_INVALID_SAML_VERSION);
    }
  }

  private static void validateSAMLResponseId(String id) {
    if (id == null || id.isBlank()) {
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_NOT_VALID_RESPONSE_ID);
    }
  }

  private static SubjectConfirmationData extractSubjectConfirmationData(Assertion assertion) {
    List<SubjectConfirmation> subjectConfirmations = assertion.getSubject()
        .getSubjectConfirmations();
    if (subjectConfirmations
        .isEmpty()) {
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_SUBJECT_CONFIRMATION_NOT_INITIALIZED);
    }
    SubjectConfirmation subjectConfirmation = subjectConfirmations.getFirst();
    if (subjectConfirmation.getMethod() == null
        || !subjectConfirmation.getMethod()
        .equals("urn:oasis:names:tc:SAML:2.0:cm:bearer")) {
      throw new SAMLValidationException(
          ErrorCode.IDP_ERROR_SUBJECT_CONFIRMATION_INVALID_METHOD_ATTRIBUTE);
    }
    SubjectConfirmationData subjectConfirmationData = subjectConfirmation
        .getSubjectConfirmationData();

    if (subjectConfirmationData == null) {
      throw new SAMLValidationException(
          ErrorCode.IDP_ERROR_SUBJECT_CONFIRMATION_DATA_NOT_FOUND);
    }
    return subjectConfirmationData;
  }

  private static void validateAuthStatement(List<AuthnStatement> authnStatements,
      AuthLevel authLevelRequest) {
    if (authnStatements == null || authnStatements.isEmpty()) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_AUTHN_STATEMENTS_MISSING_OR_EMPTY);
    }

    AuthnContext authnContext = authnStatements.getFirst().getAuthnContext();
    if (authnContext == null) {

      throw new SAMLValidationException(
          ErrorCode.IDP_ERROR_AUTHN_CONTEXT_MISSING);
    }
    AuthnContextClassRef authnContextClassRef = authnContext.getAuthnContextClassRef();

    if (authnContextClassRef == null) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_MISSING);
    }
    Element element = authnContextClassRef.getDOM();
    if (element == null || element.getTextContent() == null || element.getTextContent().isBlank()) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_EMPTY);
    }
    AuthLevel authLevelResponse = AuthLevel.authLevelFromValue(element.getTextContent().strip());
    if (authLevelResponse
        == null) {
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_INVALID_AUTHN_CONTEXT_CLASS_REF,
          ErrorCode.IDP_ERROR_INVALID_AUTHN_CONTEXT_CLASS_REF.getErrorMessage()
              + element.getTextContent().strip());
    }
    if (authLevelResponse.compareTo(authLevelRequest) < 0) {
      throw new SAMLValidationException(
          ErrorCode.IDP_ERROR_AUTHN_CONTEXT_CLASS_REF_NOT_MATCHING_REQUESTED_AUTH_LEVEL);
    }
  }

  private static void validateSubject(Subject subject) {
    if (subject == null || subject.getNameID() == null) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_SUBJECT_NOT_INITIALIZED);
    }
    NameID nameID = subject.getNameID();
    // Validate Name ID
    if (nameID.getFormat() == null) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_INVALID_NAME_ID_TYPE);
    }
    // Validate NameId format
    if (nameID.getFormat().isBlank() || !nameID.getFormat()
        .equals(NameIDType.TRANSIENT)) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_INVALID_NAME_ID_FORMAT);
    }
    // Validate NameQualifier
    if (nameID.getNameQualifier() == null || nameID.getNameQualifier()
        .isBlank()) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_INVALID_NAME_QUALIFIER);
    }

  }

  private void validateRecipient(SubjectConfirmationData subjectConfirmationData) {
    String recipient = subjectConfirmationData.getRecipient();
    if (recipient == null) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_RECIPIENT_NOT_FOUND);
    }
    if (!recipient.equals(BASE_PATH + ACS_URL)) {
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_RECIPIENT_MISMATCH,
          ErrorCode.IDP_ERROR_RECIPIENT_MISMATCH.getErrorMessage() + ": " + recipient);
    }
  }

  private void validateAudienceRestriction(List<AudienceRestriction> audienceRestrictions) {
    if (audienceRestrictions == null || audienceRestrictions.isEmpty()) {
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_AUDIENCE_RESTRICTIONS_MISSING_OR_EMPTY);
    }
    AudienceRestriction audienceRestriction = audienceRestrictions.getFirst();
    if (audienceRestriction == null
        || audienceRestriction.getAudiences().isEmpty()) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_AUDIENCE_RESTRICTIONS_MISSING_OR_EMPTY);
    }

    Audience audience = audienceRestriction.getAudiences().getFirst();
    Element element = audience.getDOM();
    if (element == null || element.getTextContent() == null) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_AUDIENCE_MISSING_OR_EMPTY);
    }
    if (!element.getTextContent().strip().equals(ENTITY_ID)) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_AUDIENCE_MISMATCH);
    }
  }

  private void validateDestination(String destination) {
    if (destination == null || destination.isBlank()) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_DESTINATION_NOT_FOUND);
    }
    if (!destination.equals(BASE_PATH + ACS_URL)) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_DESTINATION_MISMATCH);
    }
  }

  private void validateIssuer(Issuer issuer, String entityID) {
    // Check if element is missing
    if (issuer == null) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ISSUER_NOT_FOUND);
    }
    String issuerValue = issuer.getValue();
    // Check if element value is missing or blank
    if (StringUtils.isBlank(issuerValue)) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ISSUER_VALUE_BLANK);
    }
    // Check if element value is equal to IDP EntityID
    if (!issuerValue.equals(entityID)) {
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ISSUER_MISMATCH,
          ErrorCode.IDP_ERROR_ISSUER_MISMATCH.getErrorMessage() + ": " + issuer.getValue());
    }
    // Check if format attribute is valid
    if (!entityID.equalsIgnoreCase(CIE_ENTITY_ID) &&
        (issuer.getFormat() == null || !issuer.getFormat().equals(NameIDType.ENTITY))) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ISSUER_INVALID_FORMAT);
    }
  }

  private void validateIssueInstant(Instant issueInstant, Instant samlRequestIssueInstant) {
    if (issueInstant == null || issueInstant.toString().isBlank()) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ISSUE_INSTANT_MISSING);
    }
    if (!issueInstant.isAfter(samlRequestIssueInstant)) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ISSUE_INSTANT_AFTER_REQUEST);
    }
    if (!issueInstant.isBefore(Instant.now(clock))) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ISSUE_INSTANT_IN_THE_FUTURE);
    }
  }

  private void validateConditions(Conditions conditions) {
    if (conditions == null) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_MISSING_CONDITIONS);
    }
    validateNotBefore(conditions.getNotBefore());
    validateNotOnOrAfter(conditions.getNotOnOrAfter());
    validateAudienceRestriction(conditions.getAudienceRestrictions());
  }

  private void validateNotOnOrAfter(Instant notOnOrAfter) {
    if (notOnOrAfter == null) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_NOT_ON_OR_AFTER_NOT_FOUND);
    }
    checkNotOnOrAfter(notOnOrAfter);
  }

  private void validateNotBefore(Instant notBefore) {
    if (notBefore == null) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_NOT_BEFORE_NOT_FOUND);
    }
    checkNotBefore(notBefore);
  }

  private void checkNotBefore(Instant notBefore) {
    if (Instant.now(clock).compareTo(notBefore) <= 0) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_NOT_BEFORE_NOT_FOUND);
    }
  }

  private void checkNotOnOrAfter(Instant notOnOrAfter) {
    if (Instant.now(clock).compareTo(notOnOrAfter) >= 0) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_NOT_ON_OR_AFTER_EXPIRED);
    }
  }

  private void validateAssertion(Assertion assertion, String entityID,
      Set<String> requestedAttributes, Instant samlRequestIssueInstant,
      AuthLevel authLevelRequest) {

    // Check if assertion id is valid
    if (StringUtils.isBlank(assertion.getID())) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ASSERTION_ID_MISSING);
    }
    validateSAMLVersion(assertion.getVersion());
    validateIssueInstant(assertion.getIssueInstant(), samlRequestIssueInstant);
    validateSubject(assertion.getSubject());

    SubjectConfirmationData subjectConfirmationData = extractSubjectConfirmationData(assertion);
    validateRecipient(subjectConfirmationData);
    validateInResponseTo(subjectConfirmationData.getInResponseTo());
    validateNotOnOrAfter(subjectConfirmationData.getNotOnOrAfter());
    validateIssuer(assertion.getIssuer(), entityID);
    validateConditions(assertion.getConditions());
    validateAuthStatement(assertion.getAuthnStatements(), authLevelRequest);
    validateAttributeStatements(assertion, requestedAttributes, entityID);

  }

  private void validateAttributeStatements(Assertion assertion, Set<String> requestedAttributes,
      String entityID) {
    List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
    if (attributeStatements == null || attributeStatements.isEmpty()) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ATTRIBUTES_STATEMENTS_NOT_PRESENT);
    }
    AttributeStatement attributeStatement = attributeStatements.getFirst();
    if (attributeStatement.getAttributes() == null || attributeStatement.getAttributes()
        .isEmpty()) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_PRESENT);
    }
    // Validate if obtained attributes match requested attributes
    Optional<List<AttributeDTO>> attributes = samlUtils.getAttributeDTOListFromAssertion(assertion);
    if (attributes.isEmpty()) {

      throw new SAMLValidationException(
          ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_OBTAINED_FROM_SAML_ASSERTION);
    }

    Set<String> obtainedAttributes = attributes.get().stream().map(AttributeDTO::getAttributeName)
        .collect(
            Collectors.toSet());

    //SPID
    if (!entityID.equalsIgnoreCase(CIE_ENTITY_ID
    )) {
      if (!requestedAttributes.equals(obtainedAttributes)) {
        throw new SAMLValidationException(ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_MATCHING,
            ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_MATCHING.getErrorMessage() + ": " +
                obtainedAttributes + " vs. " + requestedAttributes);
      }
    }
    // CIE
    else {
      Set<String> validAttributes = new HashSet<>();
      validAttributes.addAll(requestedAttributes);
      validAttributes.remove(Identifier.spidCode.name());

      if (!obtainedAttributes.containsAll(validAttributes)) {
        throw new SAMLValidationException(
            ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_MATCHING_FOR_CIE,
            ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_MATCHING_FOR_CIE.getErrorMessage() + ": "
                + obtainedAttributes
                + " vs. " + validAttributes);
      }

      obtainedAttributes.removeAll(validAttributes);
      if (!eidasMinimumDataSet.containsAll(obtainedAttributes)) {
        throw new SAMLValidationException(
            ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_MATCHING_FOR_CIE,
            ErrorCode.IDP_ERROR_ATTRIBUTES_NOT_MATCHING_FOR_CIE.getErrorMessage() + ": "
                + obtainedAttributes
                + " vs. " + eidasMinimumDataSet);
      }
    }
  }

  @Override
  public void checkSAMLStatus(Response response) throws OneIdentityException {
    String statusCode = "";
    String statusMessage = "";
    if (response.getStatus() != null) {
      StatusCode statusCodeObject = response.getStatus().getStatusCode();
      if (statusCodeObject != null
          && statusCodeObject.getValue() != null && !statusCodeObject.getValue().isBlank()) {
        statusCode = response.getStatus().getStatusCode().getValue();
      } else {
        Log.error("SAML Status Code cannot be null");
        throw new OneIdentityException("Status Code not set.");
      }
      StatusMessage statusMessageObject = response.getStatus().getStatusMessage();
      if (statusMessageObject != null) {
        if (statusMessageObject.getValue() != null) {
          statusMessage = response.getStatus().getStatusMessage().getValue();
        }
      }
    }

    if (!statusCode.equals(StatusCode.SUCCESS)) {
      if (!statusMessage.isEmpty()) {
        Log.debug("SAML Response status code: " + statusCode
            + statusMessage);
        throw new SAMLResponseStatusException(
            ErrorCode.valueOf(statusMessage.toUpperCase().replaceAll(" ", "_")).getErrorCode());
      } else {
        Log.error(
            "SAML Status message not found for " + statusCode
                + " status code");
        throw new OneIdentityException("Status message not set.");
      }
    }
  }

  @Override
  public AuthnRequest buildAuthnRequest(String idpSSOEndpoint, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String authLevel) throws OneIdentityException {
    return this.buildAuthnRequest(idpSSOEndpoint, assertionConsumerServiceIndex,
        attributeConsumingServiceIndex, "", authLevel);
  }

  private AuthnRequest buildAuthnRequest(String idpSSOEndpoint, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String purpose, String authLevel)
      throws OneIdentityException {
    validateParameters(idpSSOEndpoint, assertionConsumerServiceIndex,
        attributeConsumingServiceIndex);

    AuthnRequest authnRequest = createAuthnRequest(idpSSOEndpoint, assertionConsumerServiceIndex,
        attributeConsumingServiceIndex, authLevel);
    try {
      Signature signature = samlUtils.buildSignature(authnRequest);
      authnRequest.setSignature(signature);
      samlUtils.marshallAndSign(authnRequest, signature);
    } catch (SAMLUtilsException e) {
      throw new GenericAuthnRequestCreationException(e);
    }

    return authnRequest;
  }

  private AuthnRequest createAuthnRequest(String idpSSOEndpoint, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String authLevel) {
    AuthnRequest authnRequest = samlUtils.buildSAMLObject(AuthnRequest.class);
    authnRequest.setIssueInstant(Instant.now());
    authnRequest.setForceAuthn(true);
    authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
    authnRequest.setID(samlUtils.generateSecureRandomId());
    authnRequest.setIssuer(samlUtils.buildIssuer());
    authnRequest.setNameIDPolicy(samlUtils.buildNameIdPolicy());
    authnRequest.setRequestedAuthnContext(samlUtils.buildRequestedAuthnContext(authLevel));
    authnRequest.setDestination(idpSSOEndpoint);
    authnRequest.setAssertionConsumerServiceIndex(assertionConsumerServiceIndex);
    authnRequest.setAttributeConsumingServiceIndex(attributeConsumingServiceIndex);
    return authnRequest;
  }

  private void validateParameters(String idpSSOEndpoint, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex) throws OneIdentityException {
    if (idpSSOEndpoint == null || assertionConsumerServiceIndex < 0
        || attributeConsumingServiceIndex < 0) {
      throw new OneIdentityException("Invalid parameters for AuthnRequest creation.");
    }
  }

  @Override
  public void validateSAMLResponse(Response samlResponse, String entityID,
      Set<String> requestedAttributes, Instant samlRequestIssueInstant,
      AuthLevel authLevelRequest) {

    Assertion assertion = extractAssertion(samlResponse);

    validateSAMLResponseId(samlResponse.getID());
    validateSAMLVersion(samlResponse.getVersion());
    validateIssueInstant(samlResponse.getIssueInstant(), samlRequestIssueInstant);
    validateInResponseTo(samlResponse.getInResponseTo());
    validateDestination(samlResponse.getDestination());
    validateIssuer(samlResponse.getIssuer(), entityID);
    validateAssertion(assertion, entityID, requestedAttributes, samlRequestIssueInstant,
        authLevelRequest);

    validateSignature(samlResponse, entityID);

  }

  private Assertion extractAssertion(Response samlResponse) {
    try {
      return samlResponse.getAssertions().getFirst();
    } catch (NoSuchElementException e) {

      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ASSERTION_NOT_FOUND);
    }
  }

  private void validateSignature(Response samlResponse, String entityID) {
    Optional<IDP> idp = getIDPFromEntityID(entityID);
    if (idp.isEmpty()) {
      throw new SAMLValidationException(ErrorCode.OI_ERROR_IDP_NOT_FOUND,
          ErrorCode.OI_ERROR_IDP_NOT_FOUND.getErrorMessage() + ":" + entityID);
    }
    try {
      samlUtils.validateSignature(samlResponse, idp.get());
    } catch (SAMLUtilsException e) {
      throw new SAMLValidationException(ErrorCode.OI_ERROR_VALIDATE_SIGNATURE_ERROR);
    }
  }

  @Override
  public Response getSAMLResponseFromString(String SAMLResponse) throws OneIdentityException {
    return samlUtils.getSAMLResponseFromString(SAMLResponse);
  }

  @Override
  public List<AttributeDTO> getAttributesFromSAMLAssertion(Assertion assertion)
      throws OneIdentityException {
    return samlUtils.getAttributeDTOListFromAssertion(assertion)
        .orElseThrow(OneIdentityException::new);
  }

  @Override
  public Optional<IDP> getIDPFromEntityID(String entityID) {
    if (entityID.equalsIgnoreCase(CIE_ENTITY_ID)) {
      return idpConnectorImpl.getIDPByEntityIDAndTimestamp(entityID, TIMESTAMP_CIE);
    }
    return idpConnectorImpl.getIDPByEntityIDAndTimestamp(entityID, TIMESTAMP_SPID);
  }
}
