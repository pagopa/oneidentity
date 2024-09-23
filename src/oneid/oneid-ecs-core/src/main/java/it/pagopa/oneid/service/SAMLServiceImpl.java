package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
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
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
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
  public AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String authLevel) throws OneIdentityException {
    return this.buildAuthnRequest(idpID, assertionConsumerServiceIndex,
        attributeConsumingServiceIndex, "", authLevel);
  }

  private AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String purpose, String authLevel)
      throws OneIdentityException {
    //TODO: add support for CIEid

    AuthnRequest authnRequest = samlUtils.buildSAMLObject(AuthnRequest.class);

    // Create AuthnRequest
    authnRequest.setIssueInstant(Instant.now());
    authnRequest.setForceAuthn(true);
    authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
    authnRequest.setID(
        samlUtils.generateSecureRandomId()); // TODO review, is it correct as a RandomID?
    authnRequest.setIssuer(samlUtils.buildIssuer());
    authnRequest.setNameIDPolicy(samlUtils.buildNameIdPolicy());
    authnRequest.setRequestedAuthnContext(samlUtils.buildRequestedAuthnContext(authLevel));

    try {
      authnRequest.setDestination(
          samlUtils.buildDestination(idpID).orElseThrow(IDPSSOEndpointNotFoundException::new));
    } catch (SAMLUtilsException e) {
      throw new OneIdentityException(e);
    }
    authnRequest.setAssertionConsumerServiceIndex(assertionConsumerServiceIndex);
    authnRequest.setAttributeConsumingServiceIndex(attributeConsumingServiceIndex);

    Signature signature = null;
    try {
      signature = samlUtils.buildSignature(authnRequest);
    } catch (SAMLUtilsException e) {
      throw new GenericAuthnRequestCreationException(e);
    }

    authnRequest.setSignature(signature);
    Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory()
        .getMarshaller(authnRequest);
    if (out != null) {
      try {
        out.marshall(authnRequest);
        Signer.signObject(signature);
      } catch (SignatureException | MarshallingException e) {
        throw new GenericAuthnRequestCreationException(e);
      }
    } else {
      throw new GenericAuthnRequestCreationException();
    }

    return authnRequest;
  }

  @Override
  public void validateSAMLResponse(Response samlResponse, String entityID)
      throws OneIdentityException {
    Log.debug("start");

    Assertion assertion;
    try {
      assertion = samlResponse.getAssertions().getFirst();
    } catch (NoSuchElementException e) {
      Log.error(
          "assertion not found");
      throw new SAMLValidationException();
    }

    SubjectConfirmationData subjectConfirmationData = assertion.getSubject()
        .getSubjectConfirmations().getLast()
        .getSubjectConfirmationData();

    if (subjectConfirmationData == null) {
      Log.error(
          "SubjectConfirmationData not found");
      throw new SAMLValidationException();
    }

    // Check if 'recipient' matches ACS URL
    if (subjectConfirmationData.getRecipient() != null) {
      if (!subjectConfirmationData.getRecipient().equals(SAMLUtilsConstants.ACS_URL)) {
        Log.error(
            "recipient parameter from Subject Confirmation Data does not matches ACS url: "
                + subjectConfirmationData.getRecipient());
        throw new SAMLValidationException();
      }
    } else {
      Log.error(
          "SubjectConfirmationData.getRecipient not found");
      throw new SAMLValidationException();
    }
    // Check if 'NotOnOrAfter' is expired
    if (subjectConfirmationData.getNotOnOrAfter() != null) {
      //TODO: consider parameterizing Instant.now()
      if (Instant.now().compareTo(subjectConfirmationData.getNotOnOrAfter()) >= 0) {
        Log.error(
            "NotOnOrAfter parameter from Subject Confirmation Data expired");
        throw new SAMLValidationException();
      }
    } else {
      Log.error(
          "SubjectConfirmationData.getConfirmationData not found");
      throw new SAMLValidationException();
    }

    // Validate SAMLResponse signature (Response and Assertion)
    Optional<IDP> idp = getIDPFromEntityID(entityID);
    if (idp.isEmpty()) {
      Log.error("IDP not found: " + entityID);
      throw new SAMLValidationException();
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

