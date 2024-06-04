package it.pagopa.oneid.service;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;

import java.util.List;

public interface SAMLService {

    AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex, int attributeConsumingServiceIndex);

    Response getSAMLResponseFromString(String SAMLResponse);

    boolean validateSAMLResponse(Response SAMLResponse, String idpID);

    List<Attribute> getAttributesFromSAMLResponse(Response SAMLResponse);

}
