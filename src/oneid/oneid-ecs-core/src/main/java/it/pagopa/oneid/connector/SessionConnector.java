package it.pagopa.oneid.connector;

import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.Session;
import it.pagopa.oneid.model.session.enums.RecordType;
import java.util.Optional;

public interface SessionConnector<T extends Session> {

  void saveSessionIfNotExists(T session) throws SessionException;

  Optional<T> findSession(String identifier, RecordType recordType) throws SessionException;

  void updateSAMLSession(String samlRequestID, String SAMLResponse) throws SessionException;


}
