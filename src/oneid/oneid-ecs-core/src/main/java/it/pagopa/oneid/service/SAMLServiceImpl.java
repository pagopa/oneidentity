package it.pagopa.oneid.service;

import static it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore.buildIssuer;
import static it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore.buildNameIdPolicy;
import static it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore.buildRequestedAuthnContext;
import static it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore.generateSecureRandomId;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.exception.SAMLResponseStatusException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;

@ApplicationScoped
public class SAMLServiceImpl implements SAMLService {

  @Inject
  SAMLUtilsExtendedCore samlUtils;

  @Override
  public void checkSAMLStatus(Response response) throws OneIdentityException {
    Log.debug("[SAMLServiceImpl.checkSAMLStatus] start");
    String statusCode = "";
    String statusMessage = "";
    if (response.getStatus() != null) {
      if (response.getStatus().getStatusCode() != null) {
        statusCode = response.getStatus().getStatusCode().getValue();
      } else {
        Log.error("[SAMLServiceImpl.checkSAMLStatus] SAML Status Code cannot be null");
        throw new OneIdentityException("Status Code not set.");
      }
      if (response.getStatus().getStatusMessage() != null) {
        statusMessage = response.getStatus().getStatusMessage().getValue();
      }
    }

    if (!statusCode.equals(StatusCode.SUCCESS)) {
      if (!statusMessage.isEmpty()) {
        Log.debug("[SAMLServiceImpl.checkSAMLStatus] SAML Response status code: " + statusCode
            + statusMessage);
        throw new SAMLResponseStatusException(statusMessage);
      } else {
        Log.error(
            "[SAMLServiceImpl.checkSAMLStatus] SAML Status message not found for " + statusCode
                + " status code");
        throw new OneIdentityException("Status message not set.");
      }
    }
  }

  @Override
  public AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String authLevel)
      throws GenericAuthnRequestCreationException, IDPSSOEndpointNotFoundException, SAMLUtilsException {
    return this.buildAuthnRequest(idpID, assertionConsumerServiceIndex,
        attributeConsumingServiceIndex, "", authLevel);
  }

  private AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String purpose, String authLevel)
      throws GenericAuthnRequestCreationException, IDPSSOEndpointNotFoundException, SAMLUtilsException {
    //TODO: add support for CIEid

    AuthnRequest authnRequest = SAMLUtilsExtendedCore.buildSAMLObject(AuthnRequest.class);

    // Create AuthnRequest
    authnRequest.setIssueInstant(Instant.now());
    authnRequest.setForceAuthn(true);
    authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
    authnRequest.setID(generateSecureRandomId()); // TODO review, is it correct as a RandomID?
    authnRequest.setIssuer(buildIssuer());
    authnRequest.setNameIDPolicy(buildNameIdPolicy());
    authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext(authLevel));

    authnRequest.setDestination(
        samlUtils.buildDestination(idpID).orElseThrow(IDPSSOEndpointNotFoundException::new));
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
      throws SAMLValidationException, SAMLUtilsException {
    Log.debug("[SAMLServiceImpl.validateSAMLResponse] start");

    // TODO is it ok to be 0-indexed?
    Assertion assertion = samlResponse.getAssertions().getFirst();

    SubjectConfirmationData subjectConfirmationData = assertion.getSubject()
        .getSubjectConfirmations().getLast()
        .getSubjectConfirmationData();

    // Check if 'recipient' matches ACS URL
    if (!subjectConfirmationData.getRecipient().equals(SAMLUtilsConstants.ACS_URL)) {
      Log.error(
          "[SAMLServiceImpl.validateSAMLResponse] Recipient parameter from Subject Confirmation Data does not matches ACS url: "
              + subjectConfirmationData.getRecipient());
      throw new SAMLValidationException();
    }
    // Check if 'NotOnOrAfter' is expired
    if (Instant.now().compareTo(subjectConfirmationData.getNotOnOrAfter()) >= 0) {
      Log.error(
          "[SAMLServiceImpl.validateSAMLResponse] NotOnOrAfter parameter from Subject Confirmation Data expired");
      throw new SAMLValidationException();
    }

    // Validate SAMLResponse signature (Response and Assertion)
    samlUtils.validateSignature(samlResponse, entityID);
  }

  @Override
  public Response getSAMLResponseFromString(String SAMLResponse) throws OneIdentityException {
    Log.debug("[getSAMLResponseFromString] start");
    return samlUtils.getSAMLResponseFromString(SAMLResponse);
  }

  @Override
  public List<Attribute> getAttributesFromSAMLResponse(Response SAMLResponse) {
    return List.of();
  }

  @Override
  public Optional<EntityDescriptor> getEntityDescriptorFromEntityID(String entityID)
      throws SAMLUtilsException {

    return samlUtils.getEntityDescriptor(entityID);

  }
}

