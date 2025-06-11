package it.pagopa.oneid.service;

import it.pagopa.oneid.connector.SessionConnectorImpl;
import jakarta.inject.Inject;

public class SessionServiceImpl implements SessionService {

  @Inject
  SessionConnectorImpl sessionConnectorImpl;

  @Override
  public void validateAuthnRequestIdCookie(String authnRequestId, String username) {
  }

}
