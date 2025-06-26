package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import org.opensaml.saml.saml2.core.AuthnRequest;

public interface SessionService {

  IDPSession validateAuthnRequestIdCookie(String authnRequestId, String clientId, String username);

  void saveIDPSession(AuthnRequest authnRequest, Client client) throws OneIdentityException;

  void setSessionAsAuthenticated(IDPSession idpSession);

  IDPSession validateAuthnRequestIdStatus(String authnRequestId, String clientId,
      IDPSessionStatus status)
      throws OneIdentityException;

  void updateIdPSession(IDPSession idpSession);
}
