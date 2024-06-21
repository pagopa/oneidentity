package it.pagopa.oneid.common.utils.producers;

import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ClientProducer {

  @Inject
  ClientConnectorImpl clientConnectorImpl;

  @ApplicationScoped
  @Produces
  Map<String, Client> clientsMap() {
    Map<String, Client> map = new HashMap<>();
    ArrayList<Client> clients =
        clientConnectorImpl.findAll().orElse(new ArrayList<>());

    clients.forEach(client -> map.put(client.getClientId(), client));

    return map;
  }
}
