package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.w3c.dom.Element;

public interface IdpMockService {

  Response createSamlResponse(AuthnRequest authnRequest) throws SAMLUtilsException;

  String getStringValue(Element element);

  AuthnRequest getAuthnRequestFromString(String authnRequest) throws OneIdentityException;

  Element getElementValueFromSamlResponse(Response samlResponse);
}
