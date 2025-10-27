package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.connector.PDVApiPlanClient;
import it.pagopa.oneid.common.connector.exception.NoMasterKeyException;
import it.pagopa.oneid.common.connector.exception.PDVException;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.dto.PDVApiKeysDTO;
import it.pagopa.oneid.common.model.dto.PDVPlanDTO;
import it.pagopa.oneid.common.model.dto.PDVValidationResponseDTO;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.model.exception.ExistingUserIdException;
import it.pagopa.oneid.common.utils.SSMConnectorUtilsImpl;
import it.pagopa.oneid.exception.InvalidPDVPlanException;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.exception.RefreshSecretException;
import it.pagopa.oneid.exception.SSMUpsertPDVException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@QuarkusTest
class ClientRegistrationServiceImplTest {

  @Inject
  ClientRegistrationServiceImpl clientRegistrationServiceImpl;

  @InjectMock
  ClientConnectorImpl clientConnectorImpl;

  @InjectMock
  @RestClient
  PDVApiPlanClient pdvApiClientMock;
  @InjectMock
  SSMConnectorUtilsImpl ssmConnectorUtilsImplMock;

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
        clientRegistrationDTO, null, null));
  }

  @Test
  void validateClientRegistrationInfo_WithPairWiseEnabled_ok() {
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
        .pairwise(true)
        .build();
    String masterKey = "key";

    when(ssmConnectorUtilsImplMock.getParameter(anyString()))
        .thenReturn(Optional.of(masterKey));
    PDVValidationResponseDTO validResp = PDVValidationResponseDTO.builder()
        .valid(true)
        .build();
    when(pdvApiClientMock.validatePDVApiKey(Mockito.any(), Mockito.any()))
        .thenReturn(validResp);

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
        clientRegistrationDTO, "dummy-key", "dummy-plan"));
  }

  @Test
  void validateClientRegistrationInfo_WithPairWiseEnabled_PDVNoValidResponse_ko() {
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
        .pairwise(true)
        .build();
    String masterKey = "key";

    when(ssmConnectorUtilsImplMock.getParameter(anyString()))
        .thenReturn(Optional.of(masterKey));
    PDVValidationResponseDTO validResp = PDVValidationResponseDTO.builder()
        .valid(false)
        .build();
    when(pdvApiClientMock.validatePDVApiKey(Mockito.any(), Mockito.any()))
        .thenReturn(validResp);

    assertThrows(InvalidPDVPlanException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO, "dummy-key", "dummy-plan"));
  }

  @Test
  void validateClientRegistrationInfo_WithPairWiseEnabled_PDVThrowsError_ko() {
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
        .pairwise(true)
        .build();
    String masterKey = "key";

    when(ssmConnectorUtilsImplMock.getParameter(anyString()))
        .thenReturn(Optional.of(masterKey));
    PDVValidationResponseDTO validResp = PDVValidationResponseDTO.builder()
        .valid(false)
        .build();
    when(pdvApiClientMock.validatePDVApiKey(Mockito.any(), Mockito.any()))
        .thenThrow(new PDVException("PDV response not ok",
            new WebApplicationException(
                Response.status(404).entity("not found").build()
            )));

    assertThrows(PDVException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO, "dummy-key", "dummy-plan"));
  }

  @Test
  void validateClientRegistrationInfo_WithPairWiseEnabled_NoPlan_ko() {
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
        .pairwise(true)
        .build();

    assertThrows(InvalidPDVPlanException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO, "dummy-key", ""));
  }

  @Test
  void validateClientRegistrationInfo_WithPairWiseEnabled_NoKey_ko() {
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
        .pairwise(true)
        .build();

    assertThrows(InvalidPDVPlanException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO, "", "dummy-plan"));
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
        clientRegistrationDTO, null, null));
  }

  @Test
  void validateClientRegistrationInfo_invalid_redirectUri() {
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of(".error"))
        .build();

    assertThrows(InvalidUriException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationDTO, null, null));
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
            clientRegistrationDTO, null, null));
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
            clientRegistrationDTO, null, null));
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
            clientRegistrationDTO, null, null));
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
            clientRegistrationDTO, null, null));
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

    when(clientConnectorImpl.findAll()).thenReturn(Optional.of(allClient));

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.saveClient(
        clientRegistrationDTO, "userId", null, null));
  }

  @Test
  void saveClient_WithPairWiseEnabled_ok() {

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
        .pairwise(true)
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
        .pairwise(true)
        .build();

    ArrayList<Client> allClient = new ArrayList<>();
    allClient.add(returnClient);

    when(clientConnectorImpl.findAll()).thenReturn(Optional.of(allClient));

    when(ssmConnectorUtilsImplMock.upsertSecureStringIfPresentOnlyIfChanged(Mockito.anyString(),
        Mockito.anyString())).thenReturn(true);

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.saveClient(
        clientRegistrationDTO, "userId", "dummy-key", "dummy-plan"));
  }

  @Test
  void saveClient_WithPairWiseEnabled_SSMError_ko() {

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
        .pairwise(true)
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
        .pairwise(true)
        .build();

    ArrayList<Client> allClient = new ArrayList<>();
    allClient.add(returnClient);

    when(clientConnectorImpl.findAll()).thenReturn(Optional.of(allClient));

    when(ssmConnectorUtilsImplMock.upsertSecureStringIfPresentOnlyIfChanged(Mockito.anyString(),
        Mockito.anyString())).thenReturn(false);

    assertThrows(SSMUpsertPDVException.class,
        () -> clientRegistrationServiceImpl.saveClient(
            clientRegistrationDTO, "userId", "dummy-key", "dummy-plan"));
  }


  @Test
  void saveClient_existingUserId_ko() {

    // given
    String existingUserId = "existingUserId";
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
    when(clientConnectorImpl.getClientByUserId(existingUserId))
        .thenReturn(Optional.of(returnClient));

    // then
    assertThrows(ExistingUserIdException.class,
        () -> clientRegistrationServiceImpl.saveClient(clientRegistrationDTO, existingUserId,
            null, null));
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
    when(clientConnectorImpl.getClientExtendedById(anyString()))
        .thenReturn(Optional.of(returnClient));

    assertNotNull(clientRegistrationServiceImpl.getClientExtendedByClientId(clientId));
  }

  @Test
  void getClientByClientId_ko() {
    //when
    when(clientConnectorImpl.getClientById(anyString()))
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
    when(clientConnectorImpl.getClientByUserId(anyString()))
        .thenReturn(Optional.of(returnClient));

    assertNotNull(clientRegistrationServiceImpl.getClientByUserId(userId));
  }

  @Test
  void getClientByUserId_ko() {
    //when
    when(clientConnectorImpl.getClientByUserId(anyString()))
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

    when(mockClient.getUserId()).thenReturn(userId);
    when(clientConnectorImpl.getClientById(clientId))
        .thenReturn(Optional.of(mockClient));

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.refreshClientSecret(clientId, userId));
  }


  @Test
  void updateClient() {
    // given
    String clientId = "client-123";
    String userId = "userIdTest";
    int attributeIndex = 42;
    long originalIssuedAt = 987654321L;
    String secret = "originalSecret";
    String salt = "originalSalt";

    ClientExtended existingClientExtended = ClientExtended.builder()
        .secret(secret) // keep original secret and salt
        .salt(salt)
        .clientId(clientId)
        .userId(userId)
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
    when(clientConnectorImpl.getClientById(clientId))
        .thenReturn(Optional.of(existingClientExtended));

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
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
        .pairwise(false)
        .build();

    when(ssmConnectorUtilsImplMock.deleteParameter(Mockito.anyString())).thenReturn(true);

    // when
    assertDoesNotThrow(() -> clientRegistrationServiceImpl.updateClientExtended(
        clientRegistrationDTO, existingClientExtended, null, null));

    // then
    verify(clientConnectorImpl).updateClientExtended(Mockito.argThat(updated ->
        updated.getClientId().equals(clientId)
            && updated.getUserId().equals(userId)
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
            && !updated.isPairwise()
            && updated.getSecret().equals(secret) // unchanged
            && updated.getSalt().equals(salt) // unchanged
    ));
  }

  @Test
  void updateClient_pairWiseEnabled_ok() {
    // given
    String clientId = "client-123";
    String userId = "userIdTest";
    int attributeIndex = 42;
    long originalIssuedAt = 987654321L;
    String secret = "originalSecret";
    String salt = "originalSalt";

    ClientExtended existingClientExtended = ClientExtended.builder()
        .secret(secret) // keep original secret and salt
        .salt(salt)
        .clientId(clientId)
        .userId(userId)
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
    when(clientConnectorImpl.getClientById(clientId))
        .thenReturn(Optional.of(existingClientExtended));

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
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

    when(ssmConnectorUtilsImplMock.upsertSecureStringIfPresentOnlyIfChanged(Mockito.anyString(),
        Mockito.anyString())).thenReturn(true);

    // when
    assertDoesNotThrow(() -> clientRegistrationServiceImpl.updateClientExtended(
        clientRegistrationDTO, existingClientExtended, "dummy-key", "dummy-plan"));

    // then
    verify(clientConnectorImpl).updateClientExtended(Mockito.argThat(updated ->
        updated.getClientId().equals(clientId)
            && updated.getUserId().equals(userId)
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
    String userId = "userIdTest";
    int attributeIndex = 42;
    long originalIssuedAt = 987654321L;

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
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
        .userId(userId)
        .secret("originalSecret")
        .salt("originalSalt")
        .clientId(clientId)
        .attributeIndex(attributeIndex)
        .clientIdIssuedAt(originalIssuedAt)
        .build();

    when(ssmConnectorUtilsImplMock.deleteParameter(Mockito.anyString())).thenReturn(true);
    
    // when
    assertDoesNotThrow(() -> clientRegistrationServiceImpl.updateClientExtended(
        clientRegistrationDTO, existingClientExtended, null, null));

    // then
    verify(clientConnectorImpl).updateClientExtended(Mockito.argThat(updated ->
        updated.getClientId().equals(clientId)
            && updated.getUserId().equals(userId)
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

    when(clientConnectorImpl.getClientById(clientId)).thenReturn(Optional.empty());
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
    when(clientConnectorImpl.getClientById(clientId))
        .thenReturn(Optional.of(mockClient));

    RefreshSecretException exception = assertThrows(RefreshSecretException.class,
        () -> clientRegistrationServiceImpl.refreshClientSecret(clientId, userId));
    assertNotNull(exception.getMessage());
    assertEquals("User ID mismatch",
        exception.getMessage());
  }

  @Test
  void getPDVPlanList_ok() {
    String apiKey = "dummyApiKey";
    PDVPlanDTO plan = PDVPlanDTO.builder().id("id").name("name").build();
    PDVApiKeysDTO expected = PDVApiKeysDTO.builder().apiKeys(List.of(plan)).build();

    when(ssmConnectorUtilsImplMock.getParameter(anyString()))
        .thenReturn(Optional.of(apiKey));
    when(pdvApiClientMock.getPDVPlans(anyString()))
        .thenReturn(expected);

    // when
    PDVApiKeysDTO result = clientRegistrationServiceImpl.getPDVPlanList();

    // then
    assertNotNull(result);
    assertEquals(expected, result);

    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    verify(pdvApiClientMock).getPDVPlans(keyCaptor.capture());
    assertEquals(apiKey, keyCaptor.getValue(), "Client must use key read from SSM");

    verify(ssmConnectorUtilsImplMock).getParameter(anyString());
    verifyNoMoreInteractions(pdvApiClientMock, ssmConnectorUtilsImplMock);
  }

  @Test
  void getPDVPlanList_noApiKey_throwsNoMasterKeyException() {
    // given
    when(ssmConnectorUtilsImplMock.getParameter(anyString()))
        .thenReturn(Optional.empty());

    // when
    NoMasterKeyException ex = assertThrows(
        NoMasterKeyException.class,
        () -> clientRegistrationServiceImpl.getPDVPlanList()
    );

    // then
    assertTrue(ex.getMessage().toLowerCase().contains("api key"), "check message");

    // no interaction with pdv
    verify(pdvApiClientMock, never()).getPDVPlans(anyString());
  }

  @Test
  void getPDVPlanList_pdvThrowsWebAppException_wrapsInPDVException() {
    // given
    String apiKey = "dummyApiKey";
    when(ssmConnectorUtilsImplMock.getParameter(anyString()))
        .thenReturn(Optional.of(apiKey));
    when(pdvApiClientMock.getPDVPlans(apiKey))
        .thenThrow(new WebApplicationException(Response.status(502).build()));

    // when
    PDVException ex = assertThrows(
        PDVException.class,
        () -> clientRegistrationServiceImpl.getPDVPlanList()
    );

    // then
    assertTrue(ex.getMessage().contains("PDV response not ok"));
    assertInstanceOf(WebApplicationException.class, ex.getCause());
  }
}
