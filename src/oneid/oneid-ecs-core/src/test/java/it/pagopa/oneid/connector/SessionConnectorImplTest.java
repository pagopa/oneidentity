package it.pagopa.oneid.connector;

import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.model.session.SAMLSession;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SessionConnectorImplTest {

  @Inject
  SessionConnectorImpl<SAMLSession> samlSessionConnectorImpl;

  @Test
    //TODO implement this
  void saveSAMLSession() {

    //given
    //SAMLSession session = new SAMLSession();

    //Executable executable = () -> samlSessionConnectorImpl.saveSession(session);

    //then
    //assertDoesNotThrow(executable);

  }
}