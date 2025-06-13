package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;

public interface SessionService {

  IDPSession validateAuthnRequestIdCookie(String authnRequestId, String username);

  void setSessionAsAuthenticated(IDPSession idpSession);

  IDPSession validateAuthnRequestIdStatus(String authnRequestId, IDPSessionStatus status)
      throws OneIdentityException;

  void updateIdPSession(IDPSession idpSession);
}
