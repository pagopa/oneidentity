package it.pagopa.oneid.service;

import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.exception.IDPSessionNotFoundException;
import it.pagopa.oneid.exception.InvalidIDPSessionStatusException;
import it.pagopa.oneid.model.IDPSession;
import jakarta.enterprise.context.Dependent;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import jakarta.inject.Inject;
import java.util.Optional;

@Dependent
public class SessionServiceImpl implements SessionService {

  @Inject
  SessionConnectorImpl sessionConnectorImpl;

  @Override
  public IDPSession validateAuthnRequestIdCookie(String authnRequestId, String username) {

    IDPSession idpSession = sessionConnectorImpl
        .getIDPSessionByAuthnRequestIdAndUsername(authnRequestId, username)
        .orElseThrow(() -> new IDPSessionNotFoundException(
            "IDP session not found for authnRequestId: " + authnRequestId + " and username: "
                + username));

    if (!IDPSessionStatus.CREDENTIALS_VALIDATED.equals(idpSession.getStatus())) {
      throw new InvalidIDPSessionStatusException("Invalid IDP session status: "
          + idpSession.getStatus() + ". Expected: " + IDPSessionStatus.CREDENTIALS_VALIDATED);
    }

    return idpSession;
  }

  @Override
  public void setSessionAsAuthenticated(IDPSession idpSession) {
    sessionConnectorImpl.updateIDPSession(idpSession,
        Optional.of(IDPSessionStatus.CREDENTIALS_VALIDATED));
  }

}
