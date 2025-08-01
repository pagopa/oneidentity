package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.exception.RefreshSecretException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class ClientRegistrationServiceImplTest {

  @Inject
  ClientRegistrationServiceImpl clientRegistrationServiceImpl;

  @InjectMock
  ClientConnectorImpl clientConnectorImpl;


  @Test
  void validateClientRegistrationInfo() {
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of("test"))
        .samlRequestedAttributes(Set.of(Identifier.name))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .localizedContentMap(new HashMap<>())
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .build();

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
        clientRegistrationDTO));
  }

  @Test
  void testValidateClientRegistrationInfo_WithMultipleUris() {
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://valid.com", "https://valid.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of("test"))
        .samlRequestedAttributes(Set.of(Identifier.name))
        .build();

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
        clientRegistrationDTO));
  }

  @Test
  void validateClientRegistrationInfo_invalid_redirectUri() {
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of(".error"))
        .build();

    assertThrows(InvalidUriException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO));
  }

  @Test
  void validateClientRegistrationInfo_invalid_logoUri() {
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("error")
        .build();

    assertThrows(InvalidUriException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO));
  }

  @Test
  void validateClientRegistrationInfo_invalid_policyUri() {
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("error")
        .build();

    assertThrows(InvalidUriException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO));
  }

  @Test
  void validateClientRegistrationInfo_invalid_tosUri() {
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("error")
        .build();

    assertThrows(InvalidUriException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO));
  }

  @Test
  void validateClientRegistrationInfo_invalid_a11yUri() {
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .a11yUri("error")
        .build();

    assertThrows(InvalidUriException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO));
  }

  @Test
  void saveClient() {

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of("test"))
        .samlRequestedAttributes(Set.of(Identifier.name))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .localizedContentMap(new HashMap<>())
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .build();

    Client returnClient = Client.builder()
        .clientId("test")
        .friendlyName("test")
        .callbackURI(Set.of("test"))
        .requestedParameters(Set.of("test"))
        .authLevel(AuthLevel.L2)
        .acsIndex(0)
        .attributeIndex(0)
        .isActive(true)
        .clientIdIssuedAt(0L)
        .logoUri("test")
        .policyUri("test")
        .tosUri("test")
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .localizedContentMap(new HashMap<>())
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .build();

    ArrayList<Client> allClient = new ArrayList<>();
    allClient.add(returnClient);

    Mockito.when(clientConnectorImpl.findAll()).thenReturn(Optional.of(allClient));

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.saveClient(
        clientRegistrationDTO));
  }

  @Test
  void getClient() {

    //given
    String clientID = "test";
    String userId = "userId-test";
    Client returnClient = Client.builder()
        .clientId(clientID)
        .userId(userId)
        .friendlyName("test")
        .callbackURI(Set.of("test"))
        .requestedParameters(Set.of("name"))
        .authLevel(AuthLevel.L2)
        .acsIndex(0)
        .attributeIndex(0)
        .isActive(true)
        .clientIdIssuedAt(0L)
        .logoUri("test")
        .policyUri("test")
        .tosUri("test")
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .localizedContentMap(new HashMap<>())
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .build();

    //when
    Mockito.when(clientConnectorImpl.getClientById(Mockito.anyString()))
        .thenReturn(Optional.of(returnClient));

    assertNotNull(clientRegistrationServiceImpl.getClient(clientID, userId));
  }

  @Test
  void refreshClientSecret_success() {
    String clientId = "client-abc";
    String userId = "user-123";
    Client mockClient = Mockito.mock(Client.class);

    Mockito.when(mockClient.getUserId()).thenReturn(userId);
    Mockito.when(clientConnectorImpl.getClientById(clientId))
        .thenReturn(Optional.of(mockClient));

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.refreshClientSecret(clientId, userId));
  }


  @Test
  void updateClient() {
    // given
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .userId("test")
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of(Identifier.name))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .localizedContentMap(new HashMap<>())
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .build();

    assertDoesNotThrow(
        () -> clientRegistrationServiceImpl.updateClientRegistrationDTO(
            clientRegistrationDTO, new ClientID(32).getValue(), 0));
  }

  @Test
  void refreshClientSecret_noClientFound() {
    String clientId = "client-abc";
    String userId = "user-123";

    Mockito.when(clientConnectorImpl.getClientById(clientId)).thenReturn(Optional.empty());
    RefreshSecretException exception = assertThrows(RefreshSecretException.class,
        () -> clientRegistrationServiceImpl.refreshClientSecret(clientId, userId));
    assertNotNull(exception.getMessage());
    assertEquals("No client found for the clientId associated to this user",
        exception.getMessage());
  }

  @Test
  void refreshClientSecret_userIdMismatch() {
    String clientId = "client-abc";
    String userId = "user-123";

    Client mockClient = Mockito.mock(Client.class);
    Mockito.when(clientConnectorImpl.getClientById(clientId)).thenReturn(Optional.of(mockClient));

    RefreshSecretException exception = assertThrows(RefreshSecretException.class,
        () -> clientRegistrationServiceImpl.refreshClientSecret(clientId, userId));
    assertNotNull(exception.getMessage());
    assertEquals("User ID mismatch",
        exception.getMessage());
  }

  @Test
  void patchClientRegistrationDTO_shouldPatchNonNullFieldsAndBooleans() {
    ClientRegistrationDTO source = ClientRegistrationDTO.builder()
        .userId("patchedUser")
        .redirectUris(Set.of("http://patched.com"))
        .clientName("patchedName")
        .defaultAcrValues(Set.of("patchedAcr"))
        .samlRequestedAttributes(Set.of(Identifier.name))
        .logoUri("http://patched.com/logo")
        .policyUri("http://patched.com/policy")
        .tosUri("http://patched.com/tos")
        .a11yUri("http://patched.com/a11y")
        .localizedContentMap(new HashMap<>())
        .requiredSameIdp(false)
        .backButtonEnabled(true)
        .spidMinors(false)
        .spidProfessionals(null) // not passed, should not change
        //.pairwise() // not passed, should not change
        .build();

    ClientRegistrationDTO target = ClientRegistrationDTO.builder()
        .userId("originalUser")
        .redirectUris(Set.of("http://original.com"))
        .clientName("originalName")
        .defaultAcrValues(Set.of("originalAcr"))
        .samlRequestedAttributes(Set.of(Identifier.name))
        .logoUri("http://original.com/logo")
        .policyUri("http://original.com/policy")
        .tosUri("http://original.com/tos")
        .a11yUri("http://original.com/a11y")
        .localizedContentMap(new HashMap<>())
        .requiredSameIdp(false)
        .backButtonEnabled(false)
        .spidMinors(true)
        .spidProfessionals(true)
        .pairwise(false)
        .build();

    clientRegistrationServiceImpl.patchClientRegistrationDTO(source, target);

    assertEquals("patchedUser", target.getUserId());
    assertEquals(Set.of("http://patched.com"), target.getRedirectUris());
    assertEquals("patchedName", target.getClientName());
    assertEquals(Set.of("patchedAcr"), target.getDefaultAcrValues());
    assertEquals(Set.of(Identifier.name), target.getSamlRequestedAttributes());
    assertEquals("http://patched.com/logo", target.getLogoUri());
    assertEquals("http://patched.com/policy", target.getPolicyUri());
    assertEquals("http://patched.com/tos", target.getTosUri());
    assertEquals("http://patched.com/a11y", target.getA11yUri());
    assertEquals(new HashMap<>(), target.getLocalizedContentMap());
    assertFalse(target.getRequiredSameIdp());
    assertTrue(target.getBackButtonEnabled());
    assertFalse(target.getSpidMinors());
    assertTrue(target.getSpidProfessionals());
    assertFalse(target.getPairwise());
  }
}
