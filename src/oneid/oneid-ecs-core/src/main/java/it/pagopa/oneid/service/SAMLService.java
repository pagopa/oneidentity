package it.pagopa.oneid.service;

import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.exception.SAMLUtilsException;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.List;
import java.util.Optional;

public interface SAMLService {

    AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex, int attributeConsumingServiceIndex, String spidLevel) throws GenericAuthnRequestCreationException, IDPSSOEndpointNotFoundException, SAMLUtilsException;

    Response getSAMLResponseFromString(String SAMLResponse);

    boolean validateSAMLResponse(Response SAMLResponse, String idpID);

    List<Attribute> getAttributesFromSAMLResponse(Response SAMLResponse);

    Optional<EntityDescriptor> getEntityDescriptorFromEntityID(String entityID) throws SAMLUtilsException;

}
