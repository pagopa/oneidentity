package it.pagopa.oneid.service;

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
    if (session != null) {
      sessionConnectorImpl.saveSession(session);
    } else {
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
