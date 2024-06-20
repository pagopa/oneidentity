package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.Session;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.Optional;
import org.opensaml.saml.saml2.core.Response;

@Dependent
public class SessionServiceImpl<T extends Session> implements SessionService<T> {

  @Inject
  SessionConnectorImpl<T> sessionConnectorImpl;

  @Override
  public void saveSession(T session) throws SessionException {
    Log.debug("[SessionServiceImpl.saveSession] start");
    if (session != null) {
      Log.debug("[SessionServiceImpl.saveSession] session successfully saved");
      sessionConnectorImpl.saveSession(session);
    } else {
      Log.debug("[SessionServiceImpl.saveSession] save failed");
      throw new SessionException();
    }
  }

  @Override
  public Optional<T> getSession(String id) {
    return Optional.empty();
  }

  @Override
  public void setSAMLResponse(String samlRequestID, Response response) {

  }
}
