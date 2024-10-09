package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.SAMLResponseStatusException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;

@ApplicationScoped
public class SAMLServiceImpl implements SAMLService {

  @Inject
  SAMLUtilsExtendedCore samlUtils;

  @Inject
  IDPConnectorImpl idpConnectorImpl;

  @Inject
  MarshallerFactory marshallerFactory;

  @ConfigProperty(name = "timestamp_spid")
  String TIMESTAMP_SPID;

  @ConfigProperty(name = "timestamp_cie")
  String TIMESTAMP_CIE;

  @ConfigProperty(name = "cie_entity_id")
  String CIE_ENTITY_ID;

  @Override
  public void checkSAMLStatus(Response response) throws OneIdentityException {
    Log.debug("start");
    String statusCode = "";
    String statusMessage = "";
    if (response.getStatus() != null) {
      if (response.getStatus().getStatusCode() != null) {
        statusCode = response.getStatus().getStatusCode().getValue();
      } else {
        Log.error("SAML Status Code cannot be null");
        throw new OneIdentityException("Status Code not set.");
      }
      if (response.getStatus().getStatusMessage() != null) {
        if (response.getStatus().getStatusMessage().getValue() != null) {
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
      marshallAndSign(authnRequest, signature);
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

  private void marshallAndSign(AuthnRequest authnRequest, Signature signature) {
    Marshaller out = marshallerFactory
        .getMarshaller(authnRequest);
    if (out == null) {
      throw new GenericAuthnRequestCreationException();
    }
    try {
      out.marshall(authnRequest);
      Signer.signObject(signature);
    } catch (SignatureException | MarshallingException e) {
      throw new GenericAuthnRequestCreationException(e);
    }
  }

  private void validateParameters(String idpSSOEndpoint, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex) throws OneIdentityException {
    if (idpSSOEndpoint == null || assertionConsumerServiceIndex < 0
        || attributeConsumingServiceIndex < 0) {
      throw new OneIdentityException("Invalid parameters for AuthnRequest creation.");
    }
  }

  @Override
  public void validateSAMLResponse(Response samlResponse, String entityID)
      throws OneIdentityException {
    Log.debug("start");

    Assertion assertion = extractAssertion(samlResponse);
    SubjectConfirmationData subjectConfirmationData = extractSubjectConfirmationData(assertion);

    validateRecipient(subjectConfirmationData);
    validateNotOnOrAfter(subjectConfirmationData);
    validateSignature(samlResponse, entityID);
  }

  private Assertion extractAssertion(Response samlResponse) {
    try {
      return samlResponse.getAssertions().getFirst();
    } catch (NoSuchElementException e) {
      Log.error("Assertion not found in SAML response");
      throw new SAMLValidationException("Assertion not found");
    }
  }

  private SubjectConfirmationData extractSubjectConfirmationData(Assertion assertion) {
    SubjectConfirmationData subjectConfirmationData = assertion.getSubject()
        .getSubjectConfirmations().getLast()
        .getSubjectConfirmationData();

    if (subjectConfirmationData == null) {
      Log.error("SubjectConfirmationData not found");
      throw new SAMLValidationException("SubjectConfirmationData not found");
    }
    return subjectConfirmationData;
  }

  private void validateRecipient(SubjectConfirmationData subjectConfirmationData) {
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

  private void validateNotOnOrAfter(SubjectConfirmationData subjectConfirmationData) {
    Instant notOnOrAfter = subjectConfirmationData.getNotOnOrAfter();
    if (notOnOrAfter == null) {
      Log.error("NotOnOrAfter parameter not found in SubjectConfirmationData");
      throw new SAMLValidationException("NotOnOrAfter not found");
    }
    if (Instant.now().compareTo(notOnOrAfter) >= 0) {
      Log.error("NotOnOrAfter parameter expired");
      throw new SAMLValidationException("NotOnOrAfter expired");
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
    Log.debug("start");
    return samlUtils.getSAMLResponseFromString(SAMLResponse);
  }

  @Override
  public List<AttributeDTO> getAttributesFromSAMLAssertion(Assertion assertion)
      throws OneIdentityException {
    Log.debug("start");
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

