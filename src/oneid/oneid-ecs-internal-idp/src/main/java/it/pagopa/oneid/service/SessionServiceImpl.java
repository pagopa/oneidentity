package it.pagopa.oneid.service;

import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.model.IDPSession;
import jakarta.inject.Inject;

public class SessionServiceImpl implements SessionService {

  @Inject
  SessionConnectorImpl sessionConnectorImpl;

  @Override
  public IDPSession validateAuthnRequestIdCookie(String authnRequestId, String username) {
    return null;
  }

}
