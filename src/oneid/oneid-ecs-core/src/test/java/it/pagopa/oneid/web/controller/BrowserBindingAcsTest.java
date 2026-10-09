package it.pagopa.oneid.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.id.State;

import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.service.BrowserBindingService;
import it.pagopa.oneid.service.ClientLookupService;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.controller.interceptors.CurrentAuthDTO;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import it.pagopa.oneid.web.dto.SAMLResponseDTO;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@QuarkusTest
class BrowserBindingAcsTest {

  @Test
  @DisplayName("Enforcement rejects an unbound ACS before writing the SAML response")
  @SuppressWarnings("unchecked")
  void givenMissingBinding_whenEnforcingAcs_thenNoSessionMutation() {
    SAMLController controller = new SAMLController();
    controller.browserBindingService = mock(BrowserBindingService.class);
    controller.cloudWatchConnectorImpl = mock(CloudWatchConnectorImpl.class);
    controller.samlSessionService = mock(SessionServiceImpl.class);
    controller.httpHeaders = mock(HttpHeaders.class);
    controller.currentAuthDTO = new CurrentAuthDTO();
    controller.currentAuthDTO.setSamlSession(mock(SAMLSession.class));
    controller.currentAuthDTO.setResponse(mock(org.opensaml.saml.saml2.core.Response.class));
    when(controller.httpHeaders.getCookies()).thenReturn(Map.of());
    when(controller.browserBindingService.enabled()).thenReturn(true);
    when(controller.browserBindingService.enforcing()).thenReturn(true);
    when(controller.browserBindingService.mode()).thenReturn(BrowserBindingService.Mode.ENFORCE);
    when(controller.browserBindingService.verify(controller.currentAuthDTO.getSamlSession(), Map.of()))
        .thenReturn(BrowserBindingService.Outcome.MISSING);

    assertThrows(GenericHTMLException.class, () -> controller.samlACS(mock(SAMLResponseDTO.class)));
    verify(controller.cloudWatchConnectorImpl).sendBrowserBindingMetricData("MISSING");
    verifyNoInteractions(controller.samlSessionService);
  }

  @ParameterizedTest
  @EnumSource(value = BrowserBindingService.Outcome.class, names = { "MISSING", "MISMATCH", "EXPIRED" })
  @DisplayName("Rejected browser binding preserves the saved callback context without mutating the session")
  @SuppressWarnings("unchecked")
  void givenRejectedBinding_whenEnforcingAcs_thenPreserveCallbackContext(
      BrowserBindingService.Outcome outcome) {
    SAMLController controller = new SAMLController();
    controller.browserBindingService = mock(BrowserBindingService.class);
    controller.cloudWatchConnectorImpl = mock(CloudWatchConnectorImpl.class);
    controller.samlSessionService = mock(SessionServiceImpl.class);
    controller.httpHeaders = mock(HttpHeaders.class);
    controller.currentAuthDTO = new CurrentAuthDTO();
    SAMLSession session = mock(SAMLSession.class);
    AuthorizationRequestDTOExtended authorizationRequest = mock(AuthorizationRequestDTOExtended.class);
    controller.currentAuthDTO.setSamlSession(session);
    controller.currentAuthDTO.setResponse(mock(org.opensaml.saml.saml2.core.Response.class));
    when(session.getAuthorizationRequestDTOExtended()).thenReturn(authorizationRequest);
    when(authorizationRequest.getRedirectUri()).thenReturn("https://client.example/callback");
    when(authorizationRequest.getState()).thenReturn("saved-state");
    when(authorizationRequest.getClientId()).thenReturn("saved-client");
    when(controller.httpHeaders.getCookies()).thenReturn(Map.of());
    when(controller.browserBindingService.enabled()).thenReturn(true);
    when(controller.browserBindingService.enforcing()).thenReturn(true);
    when(controller.browserBindingService.mode()).thenReturn(BrowserBindingService.Mode.ENFORCE);
    when(controller.browserBindingService.verify(session, Map.of())).thenReturn(outcome);

    GenericHTMLException exception = assertThrows(GenericHTMLException.class,
        () -> controller.samlACS(mock(SAMLResponseDTO.class)));

    assertEquals("https://client.example/callback", exception.getRedirectUri());
    assertEquals("saved-state", exception.getState());
    assertEquals("saved-client", exception.getClientId());
    verify(controller.cloudWatchConnectorImpl).sendBrowserBindingMetricData(outcome.name());
    verifyNoInteractions(controller.samlSessionService);
  }

  @Test
  @DisplayName("ACS success preserves callback query values and encodes state and code")
  void given_callback_query_when_completing_acs_then_preserve_values() {
    SAMLController controller = controllerWithContext();

    var response = controller.samlACS(mock(SAMLResponseDTO.class));

    assertEquals(302, response.getStatus());
    assertEquals("path=%2Farea%26x%25&code=code%2B%25&state=saved+state%26value%3D1%2B%25%7Btoken%7D",
        response.getLocation().getRawQuery());
  }

  @Test
  @DisplayName("ACS success without state keeps the legacy null state value")
  void given_absent_state_when_completing_acs_then_keep_null_state() {
    SAMLController controller = controllerWithContext();
    when(controller.currentAuthDTO.getSamlSession().getAuthorizationRequestDTOExtended().getRedirectUri())
        .thenReturn("https://client.example/callback?source=oneid&state=old");
    when(controller.oidcServiceImpl.getAuthorizationResponse(any()).getState()).thenReturn(null);

    var response = controller.samlACS(mock(SAMLResponseDTO.class));

    assertEquals("source=oneid&code=code%2B%25&state=null", response.getLocation().getRawQuery());
  }

  @Test
  @DisplayName("SAML session write failure retains the saved callback context")
  void given_saml_session_failure_when_processing_acs_then_preserve_context() throws Exception {
    SAMLController controller = controllerWithContext();
    doThrow(new SessionException()).when(controller.samlSessionService).setSAMLResponse(any(), any());

    assertSavedContext(assertThrows(GenericHTMLException.class,
        () -> controller.samlACS(mock(SAMLResponseDTO.class))));
    verifyNoInteractions(controller.samlServiceImpl, controller.oidcSessionService);
  }

  @Test
  @DisplayName("OIDC session write failure retains the saved callback context")
  void given_oidc_session_failure_when_processing_acs_then_preserve_context() throws Exception {
    SAMLController controller = controllerWithContext();
    doThrow(new SessionException()).when(controller.oidcSessionService).saveSession(any());

    assertSavedContext(assertThrows(GenericHTMLException.class,
        () -> controller.samlACS(mock(SAMLResponseDTO.class))));
  }

  @Test
  @DisplayName("Generic SAML status failure retains the saved callback context")
  void given_status_failure_when_processing_acs_then_preserve_context() throws Exception {
    SAMLController controller = controllerWithContext();
    doThrow(new OneIdentityException()).when(controller.samlServiceImpl)
        .checkSAMLStatus(any(), any(), any(), any(), any());

    assertSavedContext(assertThrows(GenericHTMLException.class,
        () -> controller.samlACS(mock(SAMLResponseDTO.class))));
    verifyNoInteractions(controller.oidcSessionService);
  }

  @Test
  @DisplayName("Missing authentication level retains the saved callback context")
  void given_missing_auth_level_when_processing_acs_then_preserve_context() {
    SAMLController controller = controllerWithContext();
    when(controller.currentAuthDTO.getSamlSession().getRequestedAuthLevel()).thenReturn(null);

    assertSavedContext(assertThrows(GenericHTMLException.class,
        () -> controller.samlACS(mock(SAMLResponseDTO.class))));
    verifyNoInteractions(controller.oidcSessionService);
  }

  @Test
  @DisplayName("A missing client returns a local error without a callback")
  void given_missing_client_when_processing_acs_then_fallback_locally() {
    SAMLController controller = controllerWithContext();
    when(controller.clientLookupService.getClientById("saved-client")).thenReturn(Optional.empty());

    GenericHTMLException exception = assertThrows(GenericHTMLException.class,
        () -> controller.samlACS(mock(SAMLResponseDTO.class)));

    assertNull(exception.getRedirectUri());
    verifyNoInteractions(controller.oidcSessionService);
  }

  private void assertSavedContext(GenericHTMLException exception) {
    assertEquals("https://client.example/callback?path=%2Farea%26x%25", exception.getRedirectUri());
    assertEquals("saved state&value=1+%{token}", exception.getState());
    assertEquals("saved-client", exception.getClientId());
  }

  @SuppressWarnings("unchecked")
  private SAMLController controllerWithContext() {
    SAMLController controller = new SAMLController();
    controller.browserBindingService = mock(BrowserBindingService.class);
    controller.cloudWatchConnectorImpl = mock(CloudWatchConnectorImpl.class);
    controller.samlSessionService = mock(SessionServiceImpl.class);
    controller.oidcSessionService = mock(SessionServiceImpl.class);
    controller.samlServiceImpl = mock(SAMLServiceImpl.class);
    controller.oidcServiceImpl = mock(OIDCServiceImpl.class);
    controller.clientLookupService = mock(ClientLookupService.class);
    controller.currentAuthDTO = new CurrentAuthDTO();
    SAMLSession session = mock(SAMLSession.class);
    AuthorizationRequestDTOExtended request = mock(AuthorizationRequestDTOExtended.class);
    controller.currentAuthDTO.setSamlSession(session);
    controller.currentAuthDTO.setResponse(mock(org.opensaml.saml.saml2.core.Response.class));
    when(session.getAuthorizationRequestDTOExtended()).thenReturn(request);
    when(session.getRequestedAuthLevel()).thenReturn(AuthLevel.L2.getValue());
    when(request.getRedirectUri()).thenReturn("https://client.example/callback?path=%2Farea%26x%25");
    when(request.getState()).thenReturn("saved state&value=1+%{token}");
    when(request.getClientId()).thenReturn("saved-client");
    when(controller.clientLookupService.getClientById("saved-client"))
        .thenReturn(Optional.of(mock(Client.class)));
    AuthorizationResponse response = mock(AuthorizationResponse.class);
    AuthorizationSuccessResponse success = mock(AuthorizationSuccessResponse.class);
    when(controller.oidcServiceImpl.getAuthorizationResponse(any())).thenReturn(response);
    when(response.toSuccessResponse()).thenReturn(success);
    when(success.getAuthorizationCode()).thenReturn(new AuthorizationCode("code+%"));
    when(response.getState()).thenReturn(new State("saved state&value=1+%{token}"));
    return controller;
  }
}
