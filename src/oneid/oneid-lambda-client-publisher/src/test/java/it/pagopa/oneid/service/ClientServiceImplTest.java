package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.ClientConnector;
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
  ClientConnector clientConnector;

  @Test
  void getAllClientsInformation_returnsMappedClients() {

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
    when(clientConnector.findAllActive()).thenReturn(Optional.of(clients));

    //then
    var result = clientServiceImpl.getAllClientsInformation();
    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(2, result.get().size());
    assertEquals("test1", result.get().get(0).getClientID());
  }

  @Test
  void getAllClientsInformation_returnsEmptyWhenConnectorHasNoClients() {
    when(clientConnector.findAllActive()).thenReturn(Optional.empty());

    var result = clientServiceImpl.getAllClientsInformation();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getAllClientsInformation_usesActiveClientsFromConnector() {
    Client activeClient = Client.builder()
        .clientId("active")
        .friendlyName("active")
        .callbackURI(Set.of("test"))
        .requestedParameters(Set.of("test"))
        .authLevel(AuthLevel.L2)
        .isActive(true)
        .build();
    when(clientConnector.findAllActive())
        .thenReturn(Optional.of(new ArrayList<>(java.util.List.of(activeClient))));

    var result = clientServiceImpl.getAllClientsInformation().orElseThrow();

    assertEquals(1, result.size());
    assertEquals("active", result.get(0).getClientID());
    Mockito.verify(clientConnector).findAllActive();
    Mockito.verify(clientConnector, Mockito.never()).findAll();
  }

  @Test
  void getAllClientsInformation_excludesProtectedAcsIndexesOnly() {
    Client protectedClient99 = Client.builder().clientId("protected-99").acsIndex(99).build();
    Client protectedClient100 = Client.builder().clientId("protected-100").acsIndex(100).build();
    Client regularEidasClient = Client.builder()
        .clientId("regular-eidas")
        .acsIndex(7)
        .eidasIndex(99)
        .build();
    when(clientConnector.findAllActive()).thenReturn(Optional.of(
        new ArrayList<>(java.util.List.of(protectedClient99, protectedClient100,
            regularEidasClient))));

    var result = clientServiceImpl.getAllClientsInformation().orElseThrow();

    assertEquals(1, result.size());
    assertEquals("regular-eidas", result.getFirst().getClientID());
    assertEquals(99, result.getFirst().getEidasIndex());
  }
}
	