package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.exception.IDPSessionNotFoundException;
import it.pagopa.oneid.exception.InvalidIDPSessionStatusException;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import jakarta.inject.Inject;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class SessionServiceImplTest {

  @Inject
  SessionServiceImpl sessionServiceImpl;

  @InjectMock
  SessionConnectorImpl sessionConnectorImpl;

  @Test
  void validateAuthnRequestIdCookie_success() {
    // Given
    String authnRequestId = "testAuthnRequestId";
    String clientId = "testClientId";
    String username = "testUsername";

    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequestId)
        .clientId(clientId)
        .username(username)
        .status(IDPSessionStatus.CREDENTIALS_VALIDATED)
        .build();

    // When
    Mockito.when(
        sessionConnectorImpl.getIDPSessionByAuthnRequestIdClientIdAndUsername(Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(
        Optional.of(idpSession)
    );

    // Then
    assertEquals(idpSession,
        sessionServiceImpl.validateAuthnRequestIdCookie(authnRequestId, clientId, username));
  }

  @Test
  void validateAuthnRequestIdCookie_notFound() {
    // Given
    String authnRequestId = "testAuthnRequestId";
    String clientId = "testClientId";
    String username = "testUsername";

    // When
    Mockito.when(
        sessionConnectorImpl.getIDPSessionByAuthnRequestIdClientIdAndUsername(Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(Optional.empty());

    // Then
    assertThrows(IDPSessionNotFoundException.class, () ->
        sessionServiceImpl.validateAuthnRequestIdCookie(authnRequestId, clientId, username));

  }

  @Test
  void validateAuthnRequestIdCookie_invalidStatus() {
    // Given
    String authnRequestId = "testAuthnRequestId";
    String clientId = "testClientId";
    String username = "testUsername";

    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequestId)
        .clientId(clientId)
        .username(username)
        .status(IDPSessionStatus.PENDING) // Invalid status for this test
        .build();

    // When
    Mockito.when(
        sessionConnectorImpl.getIDPSessionByAuthnRequestIdClientIdAndUsername(Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(
        Optional.of(idpSession)
    );

    // Then

    assertThrows(InvalidIDPSessionStatusException.class, () ->
        sessionServiceImpl.validateAuthnRequestIdCookie(authnRequestId, clientId, username));
  }

  @Test
  void setSessionAsAuthenticated_success() {
    // Given
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId("testAuthnRequestId")
        .clientId("testClientId")
        .username("testUsername")
        .status(IDPSessionStatus.PENDING)
        .build();

    // When
    sessionServiceImpl.setSessionAsAuthenticated(idpSession);

    // Then
    Mockito.verify(sessionConnectorImpl, Mockito.times(1))
        .updateIDPSession(idpSession, Optional.of(IDPSessionStatus.CREDENTIALS_VALIDATED));
  }

  @Test
  void updateIdpSession_success() {
    // Given
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId("testAuthnRequestId")
        .clientId("testClientId")
        .username("testUsername")
        .status(IDPSessionStatus.PENDING)
        .build();

    // When
    sessionServiceImpl.updateIdPSession(idpSession);

    // Then
    Mockito.verify(sessionConnectorImpl, Mockito.times(1))
        .updateIDPSession(idpSession, Optional.of(IDPSessionStatus.PENDING));

  }
}
