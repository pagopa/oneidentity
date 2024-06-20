package it.pagopa.oneid.service;

import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.Session;
import it.pagopa.oneid.model.session.enums.RecordType;
import java.util.Optional;

public interface SessionService<T extends Session> {

  void saveSession(T session) throws SessionException;

  Optional<T> getSession(String id, RecordType recordType)
      throws SessionException;

  void setSAMLResponse(String samlRequestID, String response) throws SessionException;
}
