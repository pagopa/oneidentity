package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import jakarta.inject.Inject;
import java.util.ArrayList;
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
    ClientRegistrationRequestDTO clientRegistrationRequestDTO = ClientRegistrationRequestDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of("test"))
        .samlRequestedAttributes(Set.of(Identifier.name))
        .build();

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
        clientRegistrationRequestDTO));
  }

  @Test
  void testValidateClientRegistrationInfo_WithMultipleUris() {
    ClientRegistrationRequestDTO clientRegistrationRequestDTO = ClientRegistrationRequestDTO.builder()
        .redirectUris(Set.of("http://valid.com", "https://valid.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of("test"))
        .samlRequestedAttributes(Set.of(Identifier.name))
        .build();

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
        clientRegistrationRequestDTO));
  }

  @Test
  void validateClientRegistrationInfo_invalid_redirectUri() {
    ClientRegistrationRequestDTO clientRegistrationRequestDTO = ClientRegistrationRequestDTO.builder()
        .redirectUris(Set.of(".error"))
        .build();

    assertThrows(InvalidUriException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationRequestDTO));
  }

  @Test
  void validateClientRegistrationInfo_invalid_logoUri() {
    ClientRegistrationRequestDTO clientRegistrationRequestDTO = ClientRegistrationRequestDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("error")
        .build();

    assertThrows(InvalidUriException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationRequestDTO));
  }

  @Test
  void validateClientRegistrationInfo_invalid_policyUri() {
    ClientRegistrationRequestDTO clientRegistrationRequestDTO = ClientRegistrationRequestDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("error")
        .build();

    assertThrows(InvalidUriException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationRequestDTO));
  }

  @Test
  void validateClientRegistrationInfo_invalid_tosUri() {
    ClientRegistrationRequestDTO clientRegistrationRequestDTO = ClientRegistrationRequestDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("error")
        .build();

    assertThrows(InvalidUriException.class,
        () -> clientRegistrationServiceImpl.validateClientRegistrationInfo(
            clientRegistrationRequestDTO));
  }

  @Test
  void saveClient() {

    ClientRegistrationRequestDTO clientRegistrationRequestDTO = ClientRegistrationRequestDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of("test"))
        .samlRequestedAttributes(Set.of(Identifier.name))
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
        .build();

    ArrayList<Client> allClient = new ArrayList<>();
    allClient.add(returnClient);

    Mockito.when(clientConnectorImpl.findAll()).thenReturn(Optional.of(allClient));

    assertDoesNotThrow(() -> clientRegistrationServiceImpl.saveClient(
        clientRegistrationRequestDTO));
  }

  @Test
  void getClientMetadataDTO() {

    //given
    String clientID = "test";
    Client returnClient = Client.builder()
        .clientId(clientID)
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
        .build();

    //when
    Mockito.when(clientConnectorImpl.getClientById(Mockito.anyString()))
        .thenReturn(Optional.of(returnClient));

    assertNotNull(clientRegistrationServiceImpl.getClientMetadataDTO(clientID));
  }


}