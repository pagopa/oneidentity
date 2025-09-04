package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.model.exception.ExistingUserIdException;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.exception.RefreshSecretException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
        .defaultAcrValues(Set.of("https://www.spid.gov.it/SpidL1"))
        .samlRequestedAttributes(Set.of("name"))
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
        .defaultAcrValues(Set.of("https://www.spid.gov.it/SpidL1"))
        .samlRequestedAttributes(Set.of("name"))
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
  void saveClient_ok() {

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of("test"))
        .samlRequestedAttributes(Set.of("name"))
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
  void saveClient_existingUserId_ko() {

    // given
    String existingUserId = "existingUserId";
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .userId(existingUserId)
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of("test"))
        .samlRequestedAttributes(Set.of("name"))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .localizedContentMap(new HashMap<>())
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .build();

    Client returnClient = Client.builder()
        .userId(existingUserId)
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

    // when
    Mockito.when(clientConnectorImpl.getClientByUserId(existingUserId))
        .thenReturn(Optional.of(returnClient));

    // then
    assertThrows(ExistingUserIdException.class,
        () -> clientRegistrationServiceImpl.saveClient(clientRegistrationDTO));
  }

  @Test
  void getClientByClientId() {

    //given
    String clientId = "test";
    String userId = "userId-test";
    Client returnClient = Client.builder()
        .clientId(clientId)
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

    assertNotNull(clientRegistrationServiceImpl.getClientByClientId(clientId));
  }

  @Test
  void getClientByClientId_ko() {
    //when
    Mockito.when(clientConnectorImpl.getClientById(Mockito.anyString()))
        .thenReturn(Optional.empty());

    // then
    assertThrows(ClientNotFoundException.class,
        () -> clientRegistrationServiceImpl.getClientByClientId("nonExistentUserId"));
  }

  @Test
  void getClientByUserId() {

    //given
    String clientId = "test";
    String userId = "userId-test";
    Client returnClient = Client.builder()
        .clientId(clientId)
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
    Mockito.when(clientConnectorImpl.getClientByUserId(Mockito.anyString()))
        .thenReturn(Optional.of(returnClient));

    assertNotNull(clientRegistrationServiceImpl.getClientByUserId(userId));
  }

  @Test
  void getClientByUserId_ko() {
    //when
    Mockito.when(clientConnectorImpl.getClientByUserId(Mockito.anyString()))
        .thenReturn(Optional.empty());

    // then
    assertThrows(ClientNotFoundException.class,
        () -> clientRegistrationServiceImpl.getClientByUserId("nonExistentUserId"));
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
    String clientId = "client-123";
    int attributeIndex = 42;
    long originalIssuedAt = 987654321L;
    Client existingClient = Client.builder()
        .clientId(clientId)
        .userId("test")
        .friendlyName("Old Name")
        .callbackURI(Set.of("http://old.com"))
        .requestedParameters(Set.of("name"))
        .authLevel(AuthLevel.L2)
        .acsIndex(0)
        .attributeIndex(attributeIndex)
        .isActive(true)
        .clientIdIssuedAt(originalIssuedAt)
        .logoUri("oldLogo")
        .policyUri("oldPolicy")
        .tosUri("oldTos")
        .requiredSameIdp(false)
        .a11yUri("oldA11y")
        .backButtonEnabled(false)
        .localizedContentMap(new HashMap<>())
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .build();
    Mockito.when(clientConnectorImpl.getClientById(clientId))
        .thenReturn(Optional.of(existingClient));

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .userId("test")
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("newLogo")
        .policyUri("newPolicy")
        .tosUri("newTos")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .a11yUri("newA11y")
        .backButtonEnabled(true)
        .localizedContentMap(new HashMap<>())
        .spidMinors(true)
        .spidProfessionals(true)
        .pairwise(true)
        .build();

    // when
    assertDoesNotThrow(() -> clientRegistrationServiceImpl.updateClientRegistrationDTO(
        clientRegistrationDTO, clientId, attributeIndex, originalIssuedAt));

    // then
    Mockito.verify(clientConnectorImpl).updateClient(Mockito.argThat(updated ->
        updated.getClientId().equals(clientId)
            && updated.getUserId().equals("test")
            && updated.getFriendlyName().equals("test")
            && updated.getCallbackURI().equals(Set.of("http://test.com"))
            && updated.getRequestedParameters().equals(Set.of("name"))
            && updated.getAuthLevel() == AuthLevel.L2
            && updated.getAcsIndex() == 0
            && updated.getAttributeIndex() == attributeIndex
            && updated.isActive()
            && updated.getClientIdIssuedAt() == originalIssuedAt
            && updated.getLogoUri().equals("newLogo")
            && updated.getPolicyUri().equals("newPolicy")
            && updated.getTosUri().equals("newTos")
            && !updated.isRequiredSameIdp() // default false
            && updated.getA11yUri().equals("newA11y")
            && updated.isBackButtonEnabled()
            && updated.getLocalizedContentMap().equals(new HashMap<>())
            && updated.isSpidMinors()
            && updated.isSpidProfessionals()
            && updated.isPairwise()
    ));
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
    Mockito.when(clientConnectorImpl.getClientById(clientId))
        .thenReturn(Optional.of(mockClient));

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
        .samlRequestedAttributes(Set.of("name"))
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
        .localizedContentMap(
            Map.of("default",
                Map.of("en",
                    new Client.LocalizedContent("Title", "Description", "http://test.com",
                        null, "")
                )
            ))
        .build();

    ClientRegistrationDTO target = ClientRegistrationDTO.builder()
        .userId("originalUser")
        .redirectUris(Set.of("http://original.com"))
        .clientName("originalName")
        .defaultAcrValues(Set.of("originalAcr"))
        .samlRequestedAttributes(Set.of("name"))
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
        .localizedContentMap(
            Map.of("default",
                Map.of("en",
                    new Client.LocalizedContent("Title", "Description", "http://test.com",
                        "test", "test")
                )
            ))
        .build();

    clientRegistrationServiceImpl.patchClientRegistrationDTO(source, target);

    assertEquals(Set.of("http://patched.com"), target.getRedirectUris());
    assertEquals("patchedName", target.getClientName());
    assertEquals(Set.of("patchedAcr"), target.getDefaultAcrValues());
    assertEquals(Set.of("name"), target.getSamlRequestedAttributes());
    assertEquals("http://patched.com/logo", target.getLogoUri());
    assertEquals("http://patched.com/policy", target.getPolicyUri());
    assertEquals("http://patched.com/tos", target.getTosUri());
    assertEquals("http://patched.com/a11y", target.getA11yUri());
    assertFalse(target.getRequiredSameIdp());
    assertTrue(target.getBackButtonEnabled());
    assertFalse(target.getSpidMinors());
    assertTrue(target.getSpidProfessionals());
    assertFalse(target.getPairwise());
    assertEquals(Map.of("default",
        Map.of("en",
            new Client.LocalizedContent("Title", "Description", "http://test.com",
                "test", "")
        )
    ), target.getLocalizedContentMap());
  }
}
