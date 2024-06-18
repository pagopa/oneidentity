package it.pagopa.oneid.service;

import static it.pagopa.oneid.service.utils.SAMLUtils.buildIssuer;
import static it.pagopa.oneid.service.utils.SAMLUtils.buildNameIdPolicy;
import static it.pagopa.oneid.service.utils.SAMLUtils.buildRequestedAuthnContext;
import static it.pagopa.oneid.service.utils.SAMLUtils.generateSecureRandomId;

import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.exception.SAMLUtilsException;
import it.pagopa.oneid.service.utils.SAMLUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;

@ApplicationScoped
public class SAMLServiceImpl implements SAMLService {

  @Inject
  SAMLUtils samlUtils;

  @Override
  public AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String spidLevel)
      throws GenericAuthnRequestCreationException, IDPSSOEndpointNotFoundException, SAMLUtilsException {
    return this.buildAuthnRequest(idpID, assertionConsumerServiceIndex,
        attributeConsumingServiceIndex, "", spidLevel);
  }

  private AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String purpose, String spidLevel)
      throws GenericAuthnRequestCreationException, IDPSSOEndpointNotFoundException, SAMLUtilsException {
    //TODO: add support for CIEid

    AuthnRequest authnRequest = SAMLUtils.buildSAMLObject(AuthnRequest.class);

    // Create AuthnRequest
    authnRequest.setIssueInstant(Instant.now());
    authnRequest.setForceAuthn(true);
    authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
    authnRequest.setID(generateSecureRandomId()); // TODO review, is it correct as a RandomID?
    authnRequest.setIssuer(buildIssuer());
    authnRequest.setNameIDPolicy(buildNameIdPolicy());
    authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext(spidLevel));

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
  public Response getSAMLResponseFromString(String SAMLResponse) {
    return null;
  }

  @Override
  public boolean validateSAMLResponse(Response SAMLResponse, String idpID) {
    return false;
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

