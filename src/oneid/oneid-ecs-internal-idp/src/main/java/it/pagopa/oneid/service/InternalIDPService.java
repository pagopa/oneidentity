package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.MalformedAuthnRequestException;
import it.pagopa.oneid.exception.SAMLValidationException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.w3c.dom.Element;

public interface InternalIDPService {

  AuthnRequest getAuthnRequestFromString(String authnRequest) throws MalformedAuthnRequestException;

  void validateAuthnRequest(AuthnRequest authnRequest) throws SAMLValidationException;

  Client getClientByAttributeConsumingServiceIndex(AuthnRequest authnRequest)
      throws ClientNotFoundException;

  Response createSuccessfulSamlResponse(String authnRequestId, String clientId, String username)
      throws SAMLUtilsException;

  Element getElementValueFromSamlResponse(Response samlResponse);

  String getStringValue(Element element);

  void validateUserInformation(String clientId, String username, String password)
      throws OneIdentityException;

}
