package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.model.exception.ExistingUserIdException;
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
  void getClientExtendedByClientId() {

    //given
    String clientId = "test";
    String userId = "userId-test";
    ClientExtended returnClient = ClientExtended.builder()
        .secret("secret")
        .salt("salt")
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
    Mockito.when(clientConnectorImpl.getClientExtendedById(Mockito.anyString()))
        .thenReturn(Optional.of(returnClient));

    assertNotNull(clientRegistrationServiceImpl.getClientExtendedByClientId(clientId));
  }

  @Test
  void getClientByClientId_ko() {
    //when
    Mockito.when(clientConnectorImpl.getClientById(Mockito.anyString()))
        .thenReturn(Optional.empty());

    // then
    assertThrows(ClientNotFoundException.class,
        () -> clientRegistrationServiceImpl.getClientExtendedByClientId("nonExistentUserId"));
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
    String secret = "originalSecret";
    String salt = "originalSalt";

    ClientExtended existingClientExtended = ClientExtended.builder()
        .secret(secret) // keep original secret and salt
        .salt(salt)
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
        .thenReturn(Optional.of(existingClientExtended));

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
    assertDoesNotThrow(() -> clientRegistrationServiceImpl.updateClientExtended(
        clientRegistrationDTO, existingClientExtended));

    // then
    Mockito.verify(clientConnectorImpl).updateClientExtended(Mockito.argThat(updated ->
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
            && updated.getSecret().equals(secret) // unchanged
            && updated.getSalt().equals(salt) // unchanged
    ));
  }

  @Test
  void updateClient_nullValues() {
    // given
    String clientId = "client-123";
    int attributeIndex = 42;
    long originalIssuedAt = 987654321L;

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .userId("test")
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("newLogo")
        .policyUri("newPolicy")
        .tosUri("newTos")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("spidCode"))
        .a11yUri("newA11y")
        .backButtonEnabled(true)
        .localizedContentMap(new HashMap<>())
        .spidMinors(true)
        .spidProfessionals(false)
        //.pairwise() default false
        //.requiredSameIdp() default false
        .build();

    ClientExtended existingClientExtended = ClientExtended.builder()
        .secret("originalSecret")
        .salt("originalSalt")
        .clientId(clientId)
        .attributeIndex(attributeIndex)
        .clientIdIssuedAt(originalIssuedAt)
        .build();

    // when
    assertDoesNotThrow(() -> clientRegistrationServiceImpl.updateClientExtended(
        clientRegistrationDTO, existingClientExtended));

    // then
    Mockito.verify(clientConnectorImpl).updateClientExtended(Mockito.argThat(updated ->
        updated.getClientId().equals(clientId)
            && updated.getUserId().equals("test")
            && updated.getFriendlyName().equals("test")
            && updated.getCallbackURI().equals(Set.of("http://test.com"))
            && updated.getRequestedParameters().equals(Set.of("spidCode"))
            && updated.getAuthLevel() == AuthLevel.L2
            && updated.getAcsIndex() == 0
            && updated.getAttributeIndex() == attributeIndex
            && updated.isActive()
            && updated.getClientIdIssuedAt() == originalIssuedAt
            && updated.getLogoUri().equals("newLogo")
            && updated.getPolicyUri().equals("newPolicy")
            && updated.getTosUri().equals("newTos")
            && updated.getA11yUri().equals("newA11y")
            && updated.isBackButtonEnabled()
            && updated.getLocalizedContentMap().equals(new HashMap<>())
            && updated.isSpidMinors()
            && !updated.isSpidProfessionals()
            && !updated.isRequiredSameIdp() // default false
            && !updated.isPairwise() // default false
            && updated.getSecret().equals("originalSecret") // unchanged
            && updated.getSalt().equals("originalSalt") // unchanged
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

}
