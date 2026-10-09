package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.SAMLResponseStatusException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class SAMLErrorRedirectService {

  private static final String ACCESS_DENIED = "access_denied";

  private static final Map<String, String> OAUTH_ERROR_BY_OI_ERROR = Map.of(
      ErrorCode.ERRORCODE_NR19.getErrorCode(), ACCESS_DENIED,
      ErrorCode.ERRORCODE_NR20.getErrorCode(), ACCESS_DENIED,
      ErrorCode.ERRORCODE_NR21.getErrorCode(), ACCESS_DENIED,
      ErrorCode.ERRORCODE_NR22.getErrorCode(), ACCESS_DENIED,
      ErrorCode.ERRORCODE_NR23.getErrorCode(), ACCESS_DENIED,
      ErrorCode.ERRORCODE_NR25.getErrorCode(), ACCESS_DENIED,
      ErrorCode.ERRORCODE_NR30.getErrorCode(), ACCESS_DENIED);

  private final ClientLookupService clientLookupService;

  public SAMLErrorRedirectService(ClientLookupService clientLookupService) {
    this.clientLookupService = clientLookupService;
  }

  public Optional<URI> resolveRedirect(SAMLResponseStatusException exception) {
    String oauthError = OAUTH_ERROR_BY_OI_ERROR.get(exception.getErrorCode());
    if (oauthError == null) {
      return Optional.empty();
    }

    return resolveRedirect(exception.getClientId(), exception.getRedirectUri(), exception.getState(),
        oauthError, exception.getErrorCode(), false);
  }

  public Optional<URI> resolveBrowserBindingRedirect(String clientId, String redirectUri,
      String state) {
    return resolveRedirect(clientId, redirectUri, state, ACCESS_DENIED,
        ErrorCode.GENERIC_HTML_ERROR.getErrorCode(), true);
  }

  private Optional<URI> resolveRedirect(String clientId, String redirectUri, String state,
      String oauthError, String errorDescription, boolean requireRedirectBinding) {
    try {
      Optional<Client> client = clientLookupService.getClientById(clientId);
      if (client.isEmpty() || !isDirectRedirectAllowed(client.get(), redirectUri)
          || (requireRedirectBinding && client.get().getSamlBinding() != SamlBinding.HTTP_REDIRECT)) {
        return Optional.empty();
      }

      UriBuilder redirectUriBuilder = UriBuilder.fromUri(URI.create(redirectUri))
          .replaceQueryParam("error", oauthError)
          .replaceQueryParam("error_description", errorDescription);
      if (state != null) {
        redirectUriBuilder.replaceQueryParam("state", "{oauthState}");
      } else {
        redirectUriBuilder.replaceQueryParam("state");
      }
      return Optional.of(state != null
          ? redirectUriBuilder.buildFromMap(Map.of("oauthState", state))
          : redirectUriBuilder.build());
    } catch (RuntimeException exceptionDuringRedirectResolution) {
      return Optional.empty();
    }
  }

  private boolean isDirectRedirectAllowed(Client client, String redirectUri) {
    return client.isActive()
        && client.isClientErrorRedirectEnabled()
        && client.getCallbackURI() != null
        && client.getCallbackURI().contains(redirectUri);
  }
}
