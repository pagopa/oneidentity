package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.fail;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(MyMockyTestProfile.class)
public class SessionServiceImplTest {

  @Inject
  SessionServiceImpl<SAMLSession> sessionServiceImpl;

  @Inject
  SessionConnectorImpl<SAMLSession> sessionConnectorImpl;

  @Test
  void saveSession() {
    // given
    SAMLSession session = Mockito.mock(SAMLSession.class);
    try {
      sessionServiceImpl.saveSession(session);
    } catch (Exception e) {
      fail("Unexpected exception thrown: " + e.getMessage());
    }

  }

  @Test
  void saveNullSession() {
    try {
      sessionServiceImpl.saveSession(null);
      fail("Should have thrown SessionException");
    } catch (SessionException ignored) {
    }

  }

  @Test
  void getSession() {
    // given
    String idWithValue = "withValue";
    RecordType recordType = RecordType.SAML;

    // then
    try {
      sessionServiceImpl.getSession(idWithValue, recordType);
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }

  }

  @Test
  void getSessionNotExistingItem() {
    // given
    String idWithNoValue = "withNoValue";
    RecordType recordType = RecordType.SAML;

    // then
    try {
      sessionServiceImpl.getSession(idWithNoValue, recordType);
      fail("Should have thrown SessionException");
    } catch (Exception ignored) {
    }

  }

  @Test
  void setSAMLResponse() {
    // given
    String samlRequestID = "requestID";
    String response = "test";

    // then
    try {
      sessionServiceImpl.setSAMLResponse(samlRequestID, response);
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }

  }

  @Test
  void getSAMLSessionByCode() {
    // given
    String codeWithValue = "codeWithValue";

    // then
    try {
      sessionServiceImpl.getSAMLSessionByCode(codeWithValue);
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }

  }

  @Test
  void getSAMLResponseByCode() {
    // given
    String codeWithValue = "codeWithValue";

    // then
    try {
      sessionServiceImpl.getSAMLResponseByCode(codeWithValue);
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }

  }
}
