package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.Session;
import it.pagopa.oneid.model.session.enums.RecordType;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class SessionServiceImpl<T extends Session> implements SessionService<T> {

  @Inject
  SessionConnectorImpl<T> sessionConnectorImpl;

  @Override
  public void saveSession(T session) throws SessionException {
    Log.debug("[SessionServiceImpl.saveSession] start");
    if (session != null) {
      Log.debug("[SessionServiceImpl.saveSession] session successfully saved");
      sessionConnectorImpl.saveSessionIfNotExists(session);
    } else {
      Log.error("[SessionServiceImpl.saveSession] session object is null");
      throw new SessionException();
    }
  }

  @Override
  public T getSession(String id, RecordType recordType)
      throws SessionException {
    Log.debug("[SessionServiceImpl.getSession] start");
    return sessionConnectorImpl.findSession(id, recordType).orElseThrow(SessionException::new);
  }

  @Override
  public void setSAMLResponse(String samlRequestID, String response) throws SessionException {
    Log.debug("[SessionServiceImpl.setSAMLResponse] start");
    sessionConnectorImpl.updateSAMLSession(samlRequestID, response);
  }

  @Override
  public SAMLSession getSAMLSessionByCode(String code) throws SessionException {
    Log.debug("[SessionServiceImpl.getSAMLSessionByCode] start");
    return getSAMLSessionByCodeAndRecord(code, RecordType.OIDC);
  }

  @Override
  public String getSAMLResponseByCode(String code) throws SessionException {
    Log.debug("[SessionServiceImpl.getSAMLResponseByCode] start");
    return getSAMLSessionByCodeAndRecord(code, RecordType.ACCESS_TOKEN).getSAMLResponse();
  }

  private SAMLSession getSAMLSessionByCodeAndRecord(String code, RecordType recordType)
      throws SessionException {
    Log.debug("[SessionServiceImpl.getSAMLSessionByCodeAndRecord] start");
    T oidcSession = sessionConnectorImpl.findSession(code, recordType)
        .orElseThrow(SessionException::new);
    Log.debug("[SessionServiceImpl.getSAMLSessionByCodeAndRecord] end");
    return (SAMLSession) sessionConnectorImpl.findSession(
        oidcSession.getSamlRequestID(),
        RecordType.SAML).orElseThrow(SessionException::new);
  }

}
