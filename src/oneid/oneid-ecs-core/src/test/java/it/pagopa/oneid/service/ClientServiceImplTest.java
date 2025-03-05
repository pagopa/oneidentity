package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class ClientServiceImplTest {

  @Inject
  ClientServiceImpl clientServiceImpl;

  @InjectMock
  ClientConnectorImpl clientConnectorImpl;

  @Test
  void getClientInformation() {

    //given
    String clientID = "test";
    Client returnClient = Client.builder()
        .clientId(clientID)
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

    //when
    Mockito.when(clientConnectorImpl.getClientById(Mockito.anyString()))
        .thenReturn(Optional.of(returnClient));

    //then
    assertNotNull(clientServiceImpl.getClientInformation(clientID));
  }

  @Test
  void getAllClientsInformation() {

    //given
    Client client1 = Client.builder()
        .clientId("test1")
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

    Client client2 = Client.builder()
        .clientId("test2")
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

    ArrayList<Client> clients = new ArrayList<>();
    clients.add(client1);
    clients.add(client2);

    //when
    Mockito.when(clientConnectorImpl.findAll()).thenReturn(Optional.of(clients));

    //then
    assertNotNull(clientServiceImpl.getAllClientsInformation());
  }
}