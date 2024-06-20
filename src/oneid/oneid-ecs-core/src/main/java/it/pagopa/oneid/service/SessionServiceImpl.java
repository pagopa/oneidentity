package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.Session;
import it.pagopa.oneid.model.session.enums.RecordType;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.Optional;

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
  public Optional<T> getSession(String id, RecordType recordType)
      throws SessionException {
    Log.debug("[SessionServiceImpl.getSession] start");
    Optional<T> result = sessionConnectorImpl.findSession(id, recordType);
    // TODO add different exceptions for not found and expired sessions
    if (result.isEmpty()) {
      Log.debug(
          "[SessionServiceImpl.getSession] session not found");
      throw new SessionException();
    } else {
      Log.debug("[SessionServiceImpl.getSession] session successfully found");
      return result;
    }


  }

  @Override
  public void setSAMLResponse(String samlRequestID, String response) throws SessionException {
    sessionConnectorImpl.updateSAMLSession(samlRequestID, response);
  }
}
