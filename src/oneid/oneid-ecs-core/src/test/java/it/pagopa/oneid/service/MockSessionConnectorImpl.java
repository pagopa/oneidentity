package it.pagopa.oneid.service;

import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.Session;
import it.pagopa.oneid.model.session.enums.RecordType;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import java.util.Optional;
import javax.annotation.Priority;

@Alternative
@Priority(1)
@Dependent
public class MockSessionConnectorImpl<T extends Session> extends SessionConnectorImpl<T> {

  public MockSessionConnectorImpl() {
    super();
  }

  @Override
  public void saveSessionIfNotExists(T session) {
    // do nothing
  }

  @Override
  public Optional<T> findSession(String identifier, RecordType recordType) {
    // todo: consider setting specific value as test constants
    if (identifier.equals("withValue")) {
      SAMLSession session = new SAMLSession();
      return (Optional<T>) Optional.of(session);
    } else if (identifier.equals("codeWithValue")) {
      SAMLSession session = new SAMLSession("withValue", RecordType.SAML, 0, 0, null, null);
      session.setSAMLResponse("test");
      return (Optional<T>) Optional.of(session);
    }
    return Optional.empty();
  }

  @Override
  public void updateSAMLSession(String samlRequestID, String SAMLResponse) {
    // do nothing
  }
}
