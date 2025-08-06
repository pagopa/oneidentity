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
import it.pagopa.oneid.common.model.LocalizedContent;
import it.pagopa.oneid.common.model.LocalizedContentMap;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.exception.InvalidInputSetException;
import it.pagopa.oneid.exception.InvalidLocalizedContentMapException;
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

    LocalizedContent localizedContent = LocalizedContent.builder()
        .title("titolo")
        .description("descrizione")
        .docUri("docUri")
        .supportAddress("supportAddress")
        .cookieUri("cookieUri")
        .build();
    LocalizedContentMap localizedContentMap = LocalizedContentMap.builder()
        .contentMap(Map.of("IT", Map.of("ciao", localizedContent))).build();

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of("https://www.spid.gov.it/SpidL1"))
        .samlRequestedAttributes(Set.of(Identifier.name))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .localizedContentMap(localizedContentMap)
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
  void validateClientRegistrationInfo_invalid_samlRequestedAttributes() {
    Set<Identifier> samlRequestedAttributes = Set.of();
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .samlRequestedAttributes(samlRequestedAttributes)
        .build();

    assertThrows(InvalidInputSetException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO));
  }

  @Test
  void validateClientRegistrationInfo_invalid_localizedContentMap() {
    LocalizedContentMap emptyContentMap = new LocalizedContentMap();
    emptyContentMap.setContentMap(new HashMap<>());
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .a11yUri("http://test.com")
        .localizedContentMap(emptyContentMap)
        .build();

    assertThrows(InvalidLocalizedContentMapException.class,
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
        .localizedContentMap(new LocalizedContentMap())
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
        .localizedContentMap(new LocalizedContentMap())
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
        .localizedContentMap(new LocalizedContentMap())
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
    String clientId = "client-123";
    int attributeIndex = 42;
    long originalIssuedAt = 987654321L;

    LocalizedContent newLocalizedContent = LocalizedContent.builder()
        .title("titolo")
        .description("descrizione")
        .docUri("docUri")
        .supportAddress("supportAddress")
        .cookieUri("cookieUri")
        .build();
    LocalizedContentMap newLocalizedContentMap = LocalizedContentMap.builder()
        .contentMap(Map.of("IT", Map.of("ciao", newLocalizedContent))).build();

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
        .localizedContentMap(LocalizedContentMap.builder().build())
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
        .samlRequestedAttributes(Set.of(Identifier.name))
        .a11yUri("newA11y")
        .backButtonEnabled(true)
        .localizedContentMap(newLocalizedContentMap)
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
            && updated.getLocalizedContentMap().equals(newLocalizedContentMap)
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
    Mockito.when(clientConnectorImpl.getClientById(clientId)).thenReturn(Optional.of(mockClient));

    RefreshSecretException exception = assertThrows(RefreshSecretException.class,
        () -> clientRegistrationServiceImpl.refreshClientSecret(clientId, userId));
    assertNotNull(exception.getMessage());
    assertEquals("User ID mismatch",
        exception.getMessage());
  }

  @Test
  void patchClientRegistrationDTO_shouldPatchNonNullFieldsAndBooleans() {

    LocalizedContent newLocalizedContent = LocalizedContent.builder()
        .title("titolo")
        .description("descrizione")
        .docUri("docUri")
        .supportAddress("supportAddress")
        .cookieUri("cookieUri")
        .build();
    LocalizedContentMap newLocalizedContentMap = LocalizedContentMap.builder()
        .contentMap(Map.of("IT", Map.of("ciao", newLocalizedContent))).build();

    ClientRegistrationDTO source = ClientRegistrationDTO.builder()
        .userId("patchedUser")
        .redirectUris(Set.of("http://patched.com"))
        .clientName("patchedName")
        .samlRequestedAttributes(Set.of(Identifier.name))
        .logoUri("http://patched.com/logo")
        .policyUri("http://patched.com/policy")
        .tosUri("http://patched.com/tos")
        .a11yUri("http://patched.com/a11y")
        .localizedContentMap(newLocalizedContentMap)
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
        .samlRequestedAttributes(Set.of(Identifier.name))
        .logoUri("http://original.com/logo")
        .policyUri("http://original.com/policy")
        .tosUri("http://original.com/tos")
        .a11yUri("http://original.com/a11y")
        .localizedContentMap(new LocalizedContentMap())
        .requiredSameIdp(false)
        .backButtonEnabled(false)
        .spidMinors(true)
        .spidProfessionals(true)
        .pairwise(false)
        .build();

    clientRegistrationServiceImpl.patchClientRegistrationDTO(source, target);

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
        source));
    assertEquals(Set.of("http://patched.com"), target.getRedirectUris());
    assertEquals("patchedName", target.getClientName());
    assertEquals(Set.of(Identifier.name), target.getSamlRequestedAttributes());
    assertEquals("http://patched.com/logo", target.getLogoUri());
    assertEquals("http://patched.com/policy", target.getPolicyUri());
    assertEquals("http://patched.com/tos", target.getTosUri());
    assertEquals("http://patched.com/a11y", target.getA11yUri());
    assertEquals(newLocalizedContentMap, target.getLocalizedContentMap());
    assertFalse(target.getRequiredSameIdp());
    assertTrue(target.getBackButtonEnabled());
    assertFalse(target.getSpidMinors());
    assertTrue(target.getSpidProfessionals());
    assertFalse(target.getPairwise());
  }
}
