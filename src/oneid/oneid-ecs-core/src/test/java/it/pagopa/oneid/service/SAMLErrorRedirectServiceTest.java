package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.SAMLResponseStatusException;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@QuarkusTest
class SAMLErrorRedirectServiceTest {

  private static final String CLIENT_ID = "client-test";
  private static final String CALLBACK_URI = "https://client.example/callback";

  private final ClientLookupService clientLookupService = mock(ClientLookupService.class);
  private final SAMLErrorRedirectService service = new SAMLErrorRedirectService(
      clientLookupService);

  @Test
  @DisplayName("given consent denial and enabled client when resolving then redirect with access denied")
  void given_consent_denial_and_enabled_client_when_resolving_then_redirect_with_access_denied() {
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.of(enabledClient()));

    Optional<URI> result = service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR22,
        CALLBACK_URI, "state-value"));

    assertEquals(
        "https://client.example/callback?error=access_denied&error_description=22&state=state-value",
        result.orElseThrow().toString());
  }

  @Test
  @DisplayName("given timeout and existing callback query when resolving then preserve query and state")
  void given_timeout_and_existing_callback_query_when_resolving_then_preserve_query_and_state() {
    String callbackWithQuery = CALLBACK_URI + "?source=oneid";
    Client client = enabledClient();
    client.setCallbackURI(Set.of(callbackWithQuery));
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.of(client));

    Optional<URI> result = service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR21,
        callbackWithQuery, "state with spaces"));

    assertEquals(
        "https://client.example/callback?source=oneid&error=access_denied"
            + "&error_description=21&state=state+with+spaces",
        result.orElseThrow().toString());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = ErrorCode.class, names = {
      "ERRORCODE_NR19",
      "ERRORCODE_NR20",
      "ERRORCODE_NR21",
      "ERRORCODE_NR22",
      "ERRORCODE_NR23",
      "ERRORCODE_NR25",
      "ERRORCODE_NR30"
  })
  @DisplayName("given supported SPID user error when resolving then redirect with access denied")
  void given_supported_spid_user_error_when_resolving_then_redirect_with_access_denied(
      ErrorCode errorCode) {
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.of(enabledClient()));

    Optional<URI> result = service.resolveRedirect(exception(errorCode, CALLBACK_URI, null));

    assertEquals(
        CALLBACK_URI + "?error=access_denied&error_description=" + errorCode.getErrorCode(),
        result.orElseThrow().toString());
  }

  @Test
  @DisplayName("given reserved callback query when resolving then replace reserved parameters")
  void given_reserved_callback_query_when_resolving_then_replace_reserved_parameters() {
    String callbackWithQuery = CALLBACK_URI
        + "?source=oneid&error=old&error_description=old&state=old";
    Client client = enabledClient();
    client.setCallbackURI(Set.of(callbackWithQuery));
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.of(client));

    Optional<URI> result = service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR22,
        callbackWithQuery, "current-state"));

    assertEquals(
        "https://client.example/callback?source=oneid&error=access_denied"
            + "&error_description=22&state=current-state",
        result.orElseThrow().toString());
  }

  @Test
  @DisplayName("given canceled authentication when resolving then redirect with access denied")
  void given_canceled_authentication_when_resolving_then_redirect_with_access_denied() {
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.of(enabledClient()));

    Optional<URI> result = service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR25,
        CALLBACK_URI, null));

    assertEquals(
        "https://client.example/callback?error=access_denied&error_description=25",
        result.orElseThrow().toString());
  }

  @Test
  @DisplayName("given disabled client when resolving then keep existing error page flow")
  void given_disabled_client_when_resolving_then_keep_existing_error_page_flow() {
    Client client = enabledClient();
    client.setClientErrorRedirectEnabled(false);
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.of(client));

    assertTrue(service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR22,
        CALLBACK_URI, "state-value")).isEmpty());
  }

  @Test
  @DisplayName("given inactive client when resolving then keep existing error page flow")
  void given_inactive_client_when_resolving_then_keep_existing_error_page_flow() {
    Client client = enabledClient();
    client.setActive(false);
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.of(client));

    assertTrue(service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR22,
        CALLBACK_URI, "state-value")).isEmpty());
  }

  @Test
  @DisplayName("given client without callbacks when resolving then keep existing error page flow")
  void given_client_without_callbacks_when_resolving_then_keep_existing_error_page_flow() {
    Client client = enabledClient();
    client.setCallbackURI(null);
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.of(client));

    assertTrue(service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR22,
        CALLBACK_URI, "state-value")).isEmpty());
  }

  @Test
  @DisplayName("given missing client when resolving then keep existing error page flow")
  void given_missing_client_when_resolving_then_keep_existing_error_page_flow() {
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.empty());

    assertTrue(service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR22,
        CALLBACK_URI, "state-value")).isEmpty());
  }

  @Test
  @DisplayName("given callback mismatch when resolving then keep existing error page flow")
  void given_callback_mismatch_when_resolving_then_keep_existing_error_page_flow() {
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.of(enabledClient()));

    assertTrue(service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR22,
        "https://attacker.example/callback", "state-value")).isEmpty());
  }

  @Test
  @DisplayName("given non whitelisted status when resolving then keep existing error page flow")
  void given_non_whitelisted_status_when_resolving_then_keep_existing_error_page_flow() {
    when(clientLookupService.getClientById(CLIENT_ID)).thenReturn(Optional.of(enabledClient()));

    assertTrue(service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR18,
        CALLBACK_URI, "state-value")).isEmpty());
  }

  @Test
  @DisplayName("given client lookup failure when resolving then keep existing error page flow")
  void given_client_lookup_failure_when_resolving_then_keep_existing_error_page_flow() {
    when(clientLookupService.getClientById(CLIENT_ID)).thenThrow(new RuntimeException("failure"));

    assertTrue(service.resolveRedirect(exception(ErrorCode.ERRORCODE_NR22,
        CALLBACK_URI, "state-value")).isEmpty());
  }

  private Client enabledClient() {
    return Client.builder()
        .clientId(CLIENT_ID)
        .callbackURI(Set.of(CALLBACK_URI))
        .isActive(true)
        .clientErrorRedirectEnabled(true)
        .build();
  }

  private SAMLResponseStatusException exception(ErrorCode errorCode, String redirectUri,
      String state) {
    return new SAMLResponseStatusException(errorCode, redirectUri, CLIENT_ID, "idp-test", state);
  }
}
