package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
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
  SAMLUtilsConstants samlUtilsConstants;

  @Inject
  IDPConnectorImpl idpConnectorImpl;

  @ConfigProperty(name = "timestamp_spid")
  String TIMESTAMP_SPID;

  @ConfigProperty(name = "timestamp_cie")
  String TIMESTAMP_CIE;

  @ConfigProperty(name = "cie_entity_id")
  String CIE_ENTITY_ID;

  @Inject
  SAMLServiceImpl(Clock clock) {
    this.clock = clock;
  }

  private static void validateDestination(String destination) throws OneIdentityException {
    if (destination == null || destination.isBlank()) {
      Log.error("Destination cannot be null or empty");
      throw new OneIdentityException("Destination not set.");
    }
    if (!destination.equals(SAMLUtilsConstants.ACS_URL)) {
      Log.error("Destination does not match ACS URL: " + destination);
      throw new OneIdentityException("Destination mismatch.");
    }
  }

  private static void validateInResponseTo(String inResponseTo) throws OneIdentityException {
    if (inResponseTo == null || inResponseTo.isBlank()) {
      Log.error("InResponseTo cannot be null or empty");
      throw new OneIdentityException("InResponseTo not set.");
    }
  }

  private static void validateSAMLVersion(SAMLVersion samlVersion) throws OneIdentityException {
    if (samlVersion == null || !samlVersion.equals(SAMLVersion.VERSION_20)) {
      Log.error("Version must be 2.0");
      throw new OneIdentityException("Version different from 2.0.");
    }
  }

  private static void validateSAMLResponseId(String id) throws OneIdentityException {
    if (id == null || id.isBlank()) {
      Log.error("Response ID cannot be null or empty");
      throw new OneIdentityException("Response ID not set.");
    }
  }

  private static void validateRecipient(SubjectConfirmationData subjectConfirmationData) {
    String recipient = subjectConfirmationData.getRecipient();
    if (recipient == null) {
      Log.error("Recipient parameter from Subject Confirmation Data not found");
      throw new SAMLValidationException("Recipient not found");
    }
    if (!recipient.equals(SAMLUtilsConstants.ACS_URL)) {
      Log.error("Recipient parameter does not match ACS URL: " + recipient);
      throw new SAMLValidationException("Recipient mismatch");
    }
  }

  private static SubjectConfirmationData extractSubjectConfirmationData(Assertion assertion) {
    List<SubjectConfirmation> subjectConfirmations = assertion.getSubject()
        .getSubjectConfirmations();
    if (subjectConfirmations
        .isEmpty()) {
      Log.error("SubjectConfirmation not correctly initialized");
      throw new SAMLValidationException("SubjectConfirmation not correctly initialized");
    }
    SubjectConfirmation subjectConfirmation = subjectConfirmations.getFirst();
    if (subjectConfirmation.getMethod() == null
        || !subjectConfirmation.getMethod()
        .equals("urn:oasis:names:tc:SAML:2.0:cm:bearer")) {
      Log.error("Invalid method attribute for SubjectConfirmation");
      throw new SAMLValidationException("Invalid method attribute for SubjectConfirmation");
    }
    SubjectConfirmationData subjectConfirmationData = subjectConfirmation
        .getSubjectConfirmationData();

    if (subjectConfirmationData == null) {
      Log.error("SubjectConfirmationData not found");
      throw new SAMLValidationException("SubjectConfirmationData not found");
    }
    return subjectConfirmationData;
  }

  private static void validateAuthStatement(List<AuthnStatement> authnStatements,
      AuthLevel authLevelRequest) {
    if (authnStatements == null || authnStatements.isEmpty()) {
      Log.error("AuthnStatements element is missing or empty");
      throw new SAMLValidationException("AuthnStatements element is missing or empty");
    }

    AuthnContext authnContext = authnStatements.getFirst().getAuthnContext();
    if (authnContext == null) {
      Log.error("AuthnContext element is missing");
      throw new SAMLValidationException("AuthnContext element is missing");
    }
    AuthnContextClassRef authnContextClassRef = authnContext.getAuthnContextClassRef();

    if (authnContextClassRef == null) {
      Log.error("AuthnContextClassRef element is missing");
      throw new SAMLValidationException("AuthnContextClassRef element is missing");
    }
    Element element = authnContextClassRef.getDOM();
    if (element == null || element.getTextContent() == null || element.getTextContent().isBlank()) {
      Log.error("AuthnContextClassRef element is missing or empty");
      throw new SAMLValidationException("AuthnContextClassRef element is missing or empty");
    }
    AuthLevel authLevelResponse = AuthLevel.authLevelFromValue(element.getTextContent().strip());
    if (authLevelResponse
        == null) {
      Log.error(
          "Invalid AuthnContextClassRef value: " + element.getTextContent().strip());
      throw new SAMLValidationException("Invalid AuthnContextClassRef value");
    }
    if (authLevelResponse.compareTo(authLevelRequest) < 0) {
      Log.error("AuthnContextClassRef value does not match the requested AuthLevel");
      throw new SAMLValidationException(
          "AuthnContextClassRef value does not match the requested AuthLevel");
    }
  }

  private static void validateAudienceRestriction(List<AudienceRestriction> audienceRestrictions) {
    if (audienceRestrictions == null || audienceRestrictions.isEmpty()) {
      Log.error("Audience Restrictions element is missing or empty");
      throw new SAMLValidationException("Audience Restrictions element is missing or empty");
    }
    AudienceRestriction audienceRestriction = audienceRestrictions.getFirst();
    if (audienceRestriction == null
        || audienceRestriction.getAudiences().isEmpty()) {
      Log.error("Audience element is missing or empty");
      throw new SAMLValidationException("Audience element is missing or empty");
    }

    Audience audience = audienceRestriction.getAudiences().getFirst();
    Element element = audience.getDOM();
    if (element == null || element.getTextContent() == null || !element.getTextContent().strip()
        .equals(SAMLUtilsConstants.SERVICE_PROVIDER_URI)) {
      Log.error("Audience parameter not equal to service provider entity ID");
      throw new SAMLValidationException("Audience mismatch");
    }
  }

  private static void validateSubject(Subject subject) {
    if (subject == null || subject.getNameID() == null) {
      Log.error("Subject not correctly initialized");
      throw new SAMLValidationException("Subject not correctly initialized");
    }
    NameID nameID = subject.getNameID();
    // Validate Name ID
    if (nameID == null || nameID.getFormat() == null) {
      Log.error("Invalid Name ID type for Subject");
      throw new SAMLValidationException("Invalid Name ID type for Subject");
    }
    // Validate NameId format
    if (nameID.getFormat().isBlank() || !nameID.getFormat()
        .equals(NameIDType.TRANSIENT)) {
      Log.error("Invalid NameId format for Subject");
      throw new SAMLValidationException("Invalid NameId format for Subject");
    }
    // Validate NameQualifier
    if (nameID.getNameQualifier() == null || nameID.getNameQualifier()
        .isBlank()) {
      Log.error("Invalid NameQualifier for Subject");
      throw new SAMLValidationException("Invalid NameQualifier for Subject");
    }

  }

  private void validateIssuer(Issuer issuer, String entityID) {
    // Check if element is missing
    if (issuer == null) {
      Log.error("Issuer not found");
      throw new SAMLValidationException("Issuer not found");
    }
    String issuerValue = issuer.getValue();
    // Check if element value is missing or blank
    if (issuerValue == null || issuerValue.isBlank()) {
      Log.error("Issuer value is blank");
      throw new SAMLValidationException("Issuer value is blank");
    }
    // Check if element value is equal to IDP EntityID
    if (!issuerValue.equals(entityID)) {
      Log.error("Issuer value does not match IDP EntityID: " + issuer.getValue());
      throw new SAMLValidationException("Issuer mismatch");
    }
    // Check if format attribute is valid
    if (!entityID.equalsIgnoreCase(CIE_ENTITY_ID) &&
        (issuer.getFormat() == null || !issuer.getFormat().equals(NameIDType.ENTITY))) {
      Log.error("Invalid format attribute for Issuer element");
      throw new SAMLValidationException("Invalid format attribute for Issuer element");
    }
  }

  private void validateIssueInstant(Instant issueInstant, Instant samlRequestIssueInstant)
      throws OneIdentityException {
    if (issueInstant == null || issueInstant.toString().isBlank()) {
      Log.error("Issue Instant cannot be null or empty");
      throw new OneIdentityException("Issue Instant not set.");
    }
    if (!issueInstant.isAfter(samlRequestIssueInstant)) {
      Log.error("Issue Instant must be after the request's Issue Instant");
      throw new OneIdentityException("Issue Instant is not after the request's Issue Instant.");
    }
    if (!issueInstant.isBefore(Instant.now(clock))) {
      Log.error("Issue Instant must be before current time");
      throw new OneIdentityException("Issue Instant is not before current time.");
    }
  }

  private void validateConditions(Conditions conditions) {
    if (conditions == null) {
      Log.error("Conditions element is missing");
      throw new SAMLValidationException("Conditions element is missing");
    }
    validateNotBefore(conditions.getNotBefore());
    validateNotOnOrAfter(conditions.getNotOnOrAfter());
    validateAudienceRestriction(conditions.getAudienceRestrictions());
  }

  private void validateNotOnOrAfter(Instant notOnOrAfter) {
    if (notOnOrAfter == null) {
      Log.error("NotOnOrAfter parameter not found");
      throw new SAMLValidationException("NotOnOrAfter not found");
    }
    checkNotOnOrAfter(notOnOrAfter);
  }

  private void validateNotBefore(Instant notBefore) {
    if (notBefore == null) {
      Log.error("NotBefore parameter not found");
      throw new SAMLValidationException("NotBefore not found");
    }
    checkNotBefore(notBefore);
  }

  private void checkNotBefore(Instant notBefore) {
    if (Instant.now(clock).compareTo(notBefore) <= 0) {
      Log.error("NotBefore parameter is in the future");
      throw new SAMLValidationException("NotBefore is in the future");
    }
  }

  private void checkNotOnOrAfter(Instant notOnOrAfter) {
    if (Instant.now(clock).compareTo(notOnOrAfter) >= 0) {
      Log.error("NotOnOrAfter parameter expired");
      throw new SAMLValidationException("NotOnOrAfter expired");
    }
  }

  private void validateAssertion(Assertion assertion, String entityID,
      Set<String> requestedAttributes, Instant samlRequestIssueInstant,
      AuthLevel authLevelRequest) {

    // Check if assertion id is valid
    if (assertion.getID() == null || assertion.getID().isBlank()) {
      Log.error("Assertion ID is missing");
      throw new SAMLValidationException("Assertion ID is missing");
    }
    try {
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

    } catch (OneIdentityException e) {
      throw new SAMLValidationException(e);
    }
  }

  private void validateAttributeStatements(Assertion assertion, Set<String> requestedAttributes,
      String entityID) {
    List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
    if (attributeStatements == null || attributeStatements.isEmpty()) {
      Log.error("AttributeStatements element is missing or empty");
      throw new SAMLValidationException("AttributeStatements element is missing or empty");
    }
    AttributeStatement attributeStatement = attributeStatements.getFirst();
    if (attributeStatement.getAttributes() == null || attributeStatement.getAttributes()
        .isEmpty()) {
      Log.error("Attributes element is missing or empty");
      throw new SAMLValidationException("Attributes element is missing or empty");
    }
    // Validate if obtained attributes match requested attributes
    Optional<List<AttributeDTO>> attributes = samlUtils.getAttributeDTOListFromAssertion(assertion);
    if (attributes.isEmpty()) {
      Log.error("Attributes not obtained from SAML assertion");
      throw new SAMLValidationException("Attributes not obtained from SAML assertion");
    }

    Set<String> obtainedAttributes = attributes.get().stream().map(AttributeDTO::getAttributeName)
        .collect(
            Collectors.toSet());

    //SPID
    if (!entityID.equalsIgnoreCase(CIE_ENTITY_ID
    )) {
      if (!requestedAttributes.equals(obtainedAttributes)) {
        Log.error("Obtained attributes do not match requested attributes: "
            + obtainedAttributes + " vs. " + requestedAttributes);
        throw new SAMLValidationException("Obtained attributes do not match requested attributes");
      }
    }
    // CIE
    else {
      Set<String> validAttributes = new HashSet<>();
      validAttributes.addAll(requestedAttributes);
      validAttributes.addAll(eidasMinimumDataSet);
      validAttributes.remove(String.valueOf(Identifier.spidCode));

      if (!validAttributes.containsAll(obtainedAttributes)) {
        Log.error(
            "Obtained attributes do not match requested attributes for CIE: " + obtainedAttributes
                + " vs. " + validAttributes);
        throw new SAMLValidationException(
            "Obtained attributes do not match requested attributes for CIE");
      }

      obtainedAttributes.removeAll(validAttributes);
      if (!eidasMinimumDataSet.containsAll(obtainedAttributes)) {
        Log.error(
            "Obtained attributes do not match requested attributes for CIE: " + obtainedAttributes
                + " vs. " + eidasMinimumDataSet);
        throw new SAMLValidationException(
            "Obtained attributes do not match requested attributes for CIE");
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

    try {
      validateSAMLResponseId(samlResponse.getID());
      validateSAMLVersion(samlResponse.getVersion());
      validateIssueInstant(samlResponse.getIssueInstant(), samlRequestIssueInstant);
      validateInResponseTo(samlResponse.getInResponseTo());
      validateDestination(samlResponse.getDestination());
      validateIssuer(samlResponse.getIssuer(), entityID);
      validateAssertion(assertion, entityID, requestedAttributes, samlRequestIssueInstant,
          authLevelRequest);

      validateSignature(samlResponse, entityID);


    } catch (OneIdentityException e) {
      throw new SAMLValidationException(e);
    }
  }

  private Assertion extractAssertion(Response samlResponse) {
    try {
      return samlResponse.getAssertions().getFirst();
    } catch (NoSuchElementException e) {
      Log.error("Assertion not found in SAML response");
      throw new SAMLValidationException("Assertion not found");
    }
  }

  private void validateSignature(Response samlResponse, String entityID)
      throws OneIdentityException {
    Optional<IDP> idp = getIDPFromEntityID(entityID);
    if (idp.isEmpty()) {
      Log.error("IDP not found for entity ID: " + entityID);
      throw new SAMLValidationException("IDP not found");
    }
    try {
      samlUtils.validateSignature(samlResponse, idp.get());
    } catch (SAMLUtilsException e) {
      throw new OneIdentityException(e);
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

