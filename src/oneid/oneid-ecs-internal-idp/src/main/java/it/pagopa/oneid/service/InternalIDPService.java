package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.w3c.dom.Element;

public interface InternalIDPService {

  AuthnRequest getAuthnRequestFromString(String authnRequest) throws OneIdentityException;

  void validateAuthnRequest(AuthnRequest authnRequest) throws OneIdentityException;

  Client getClientByAttributeConsumingServiceIndex(AuthnRequest authnRequest)
      throws OneIdentityException;

  Response createSuccessfulSamlResponse(String authnRequestId, String clientId, String username)
      throws SAMLUtilsException;

  Element getElementValueFromSamlResponse(Response samlResponse);

  String getStringValue(Element element);

  void validateUserInformation(String clientId, String username, String password)
      throws OneIdentityException;

}
