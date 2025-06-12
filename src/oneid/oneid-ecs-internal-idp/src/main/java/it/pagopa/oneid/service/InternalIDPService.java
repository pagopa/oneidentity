package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;

public interface InternalIDPService {

  AuthnRequest getAuthnRequestFromString(String authnRequest) throws OneIdentityException;

  void validateAuthnRequest(AuthnRequest authnRequest) throws OneIdentityException;

  void saveUserSession(AuthnRequest authnRequest) throws OneIdentityException;
  Client getClientByAttributeConsumingServiceIndex(AuthnRequest authnRequest);

  Response createSuccessfulSamlResponse(String authnRequestId, String clientId, String username)
      throws SAMLUtilsException;

}
