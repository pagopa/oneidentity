package it.pagopa.oneid.connector;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import java.util.Optional;

public interface SessionConnector {

  void saveIDPSessionIfNotExists(IDPSession idpSession) throws OneIdentityException;

  Optional<IDPSession> getIDPSessionByAuthnRequestIdClientIdAndUsername(String authnRequestId,
      String clientId,
      String username);

  void updateIDPSession(IDPSession idpSession, Optional<IDPSessionStatus> previousStatus);

  Optional<IDPSession> findIDPSessionByAuthnRequestId(String authnRequestId);

  void updateIDPSession(IDPSession idpSession);
}
