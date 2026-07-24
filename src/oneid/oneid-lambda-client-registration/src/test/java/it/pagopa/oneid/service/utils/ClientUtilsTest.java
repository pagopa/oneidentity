package it.pagopa.oneid.service.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ClientUtilsTest {

  @Test
  @DisplayName("given omitted redirect flag when converting DTO then disable client error redirects")
  void given_omitted_redirect_flag_when_converting_dto_then_disable_client_error_redirects() {
    Client client = ClientUtils.convertClientRegistrationDTOToClient(clientRegistrationDTO(null),
        "user-id");

    assertFalse(client.isClientErrorRedirectEnabled());
  }

  @Test
  @DisplayName("given enabled redirect flag when converting then preserve it in both DTO and client")
  void given_enabled_redirect_flag_when_converting_then_preserve_it_in_both_dto_and_client() {
    Client client = ClientUtils.convertClientRegistrationDTOToClient(clientRegistrationDTO(true),
        "user-id");

    assertTrue(client.isClientErrorRedirectEnabled());
    assertTrue(ClientUtils.convertClientToClientRegistrationDTO(client)
        .getClientErrorRedirectEnabled());
  }

  @Test
  @DisplayName("given omitted or changed redirect flag when updating then report only explicit changes")
  void given_omitted_or_changed_redirect_flag_when_updating_then_report_only_explicit_changes() {
    Client existingClient = Client.builder()
        .friendlyName("client-name")
        .callbackURI(Set.of("https://client.example/callback"))
        .requestedParameters(Set.of("name"))
        .authLevel(AuthLevel.L2)
        .samlBinding(SamlBinding.HTTP_POST)
        .clientErrorRedirectEnabled(true)
        .build();

    ClientRegistrationDTO omittedFlag = ClientUtils.convertClientToClientRegistrationDTO(existingClient);
    omittedFlag.setClientErrorRedirectEnabled(null);
    assertTrue(ClientUtils.getUpdateMessage(omittedFlag, existingClient).isEmpty());

    ClientRegistrationDTO unchangedFlag = ClientUtils.convertClientToClientRegistrationDTO(
        existingClient);
    assertTrue(ClientUtils.getUpdateMessage(unchangedFlag, existingClient).isEmpty());

    ClientRegistrationDTO disabledFlag = ClientUtils.convertClientToClientRegistrationDTO(existingClient);
    disabledFlag.setClientErrorRedirectEnabled(false);
    assertEquals("ClientErrorRedirectEnabled; ",
        ClientUtils.getUpdateMessage(disabledFlag, existingClient).orElseThrow());
  }

  private ClientRegistrationDTO clientRegistrationDTO(Boolean clientErrorRedirectEnabled) {
    return ClientRegistrationDTO.builder()
        .redirectUris(Set.of("https://client.example/callback"))
        .clientName("client-name")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .clientErrorRedirectEnabled(clientErrorRedirectEnabled)
        .build();
  }
}
