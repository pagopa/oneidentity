package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.utils.producers.ClientProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.ws.rs.Produces;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Priority;

@Alternative
@Priority(1)
@Dependent
public class MockClientProducer extends ClientProducer {

  @ApplicationScoped
  @Produces
  Map<String, Client> clientsMap() {
    Map<String, Client> map = new HashMap<>();
    ArrayList<Client> clients = new ArrayList<>();
    clients.add(
        new Client("test", "test", Set.of("foo.bar"), Set.of("test"), AuthLevel.L2, 0, 0, true, 0,
            "test", "test", "test"));

    clients.forEach(client -> map.put(client.getClientId(), client));

    return map;
  }
}
