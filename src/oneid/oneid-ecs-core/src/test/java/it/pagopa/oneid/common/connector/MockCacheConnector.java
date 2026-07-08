package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.Client;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Alternative
@ApplicationScoped
@Priority(1)
public class MockCacheConnector implements CacheConnector {

  private final Map<String, Client> clients = new ConcurrentHashMap<>();

  @Override
  public Optional<Client> getByClientId(String clientId) {
    return Optional.ofNullable(clients.get(clientId));
  }

  @Override
  public void setClient(Client client) {
    if (client == null || client.getClientId() == null || client.getClientId().isBlank()) {
      return;
    }

    clients.put(client.getClientId(), client);
  }

  @Override
  public void deleteClient(String clientId) {
    if (clientId == null || clientId.isBlank()) {
      return;
    }

    clients.remove(clientId);
  }
}
