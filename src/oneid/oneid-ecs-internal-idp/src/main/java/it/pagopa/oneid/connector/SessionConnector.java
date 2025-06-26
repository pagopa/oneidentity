package it.pagopa.oneid.connector;

import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import java.util.Optional;

public interface SessionConnector {

  void saveIDPSessionIfNotExists(IDPSession idpSession);

  Optional<IDPSession> getIDPSessionByAuthnRequestIdClientIdAndUsername(String authnRequestId,
      String clientId,
      String username);

  void updateIDPSession(IDPSession idpSession, Optional<IDPSessionStatus> previousStatus);

  Optional<IDPSession> findIDPSessionByAuthnRequestIdAndClientId(String authnRequestId,
      String clientId);
}
