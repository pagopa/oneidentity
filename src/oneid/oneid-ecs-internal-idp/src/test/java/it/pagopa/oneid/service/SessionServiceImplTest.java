package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.exception.IDPSessionNotFoundException;
import it.pagopa.oneid.exception.InvalidIDPSessionStatusException;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;

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
            Mockito.any(), Mockito.any()))
        .thenReturn(
            Optional.of(idpSession));

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
            Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());

    // Then
    assertThrows(IDPSessionNotFoundException.class,
        () -> sessionServiceImpl.validateAuthnRequestIdCookie(authnRequestId, clientId, username));

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
            Mockito.any(), Mockito.any()))
        .thenReturn(
            Optional.of(idpSession));

    // Then

    assertThrows(InvalidIDPSessionStatusException.class,
        () -> sessionServiceImpl.validateAuthnRequestIdCookie(authnRequestId, clientId, username));
  }

  @Test
  void setSessionAsAuthenticated_OrDenied_success() {
    // Given
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId("testAuthnRequestId")
        .clientId("testClientId")
        .username("testUsername")
        .status(IDPSessionStatus.PENDING)
        .build();

    // When
    sessionServiceImpl.setSessionAsAuthenticatedOrDenied(idpSession);

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

  @Test
  @DisplayName("given_authn_request_with_requested_authn_context_when_saveIDPSession_then_stores_auth_level")
  void saveIDPSession_withRequestedAuthnContext_storesRequestedAuthLevel() {
    // Given
    AuthnRequest authnRequest = Mockito.mock(AuthnRequest.class);
    RequestedAuthnContext rac = Mockito.mock(RequestedAuthnContext.class);
    AuthnContextClassRef classRef = Mockito.mock(AuthnContextClassRef.class);
    Client client = Mockito.mock(Client.class);

    Mockito.when(authnRequest.getID()).thenReturn("testId");
    Mockito.when(authnRequest.getRequestedAuthnContext()).thenReturn(rac);
    Mockito.when(rac.getAuthnContextClassRefs()).thenReturn(List.of(classRef));
    Mockito.when(classRef.getURI()).thenReturn("https://www.spid.gov.it/SpidL2");
    Mockito.when(client.getClientId()).thenReturn("testClientId");
    Mockito.when(client.isSpidMinors()).thenReturn(false);

    // When
    sessionServiceImpl.saveIDPSession(authnRequest, client, "relayState");

    // Then
    ArgumentCaptor<IDPSession> captor = ArgumentCaptor.forClass(IDPSession.class);
    Mockito.verify(sessionConnectorImpl).saveIDPSessionIfNotExists(captor.capture());
    assertEquals("https://www.spid.gov.it/SpidL2", captor.getValue().getRequestedAuthLevel());
  }

  @Test
  @DisplayName("given_authn_request_without_requested_authn_context_when_saveIDPSession_then_null_auth_level")
  void saveIDPSession_withoutRequestedAuthnContext_storesNullAuthLevel() {
    // Given
    AuthnRequest authnRequest = Mockito.mock(AuthnRequest.class);
    Client client = Mockito.mock(Client.class);

    Mockito.when(authnRequest.getID()).thenReturn("testId");
    Mockito.when(authnRequest.getRequestedAuthnContext()).thenReturn(null);
    Mockito.when(client.getClientId()).thenReturn("testClientId");
    Mockito.when(client.isSpidMinors()).thenReturn(false);

    // When
    sessionServiceImpl.saveIDPSession(authnRequest, client, "relayState");

    // Then
    ArgumentCaptor<IDPSession> captor = ArgumentCaptor.forClass(IDPSession.class);
    Mockito.verify(sessionConnectorImpl).saveIDPSessionIfNotExists(captor.capture());
    assertNull(captor.getValue().getRequestedAuthLevel());
  }
}
