package it.pagopa.oneid.service;

import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.Session;
import java.util.Optional;
import org.opensaml.saml.saml2.core.Response;

public interface SessionService<T extends Session> {

  void saveSession(T session) throws SessionException;

  Optional<T> getSession(String id, RecordType recordType)
      throws SessionException;

  void setSAMLResponse(String samlRequestID, Response response);
}
