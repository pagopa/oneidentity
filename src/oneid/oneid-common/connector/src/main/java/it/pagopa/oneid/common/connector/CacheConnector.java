package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.Client;
import java.util.Optional;

public interface CacheConnector {

  Optional<Client> getByClientId(String clientId);

  void setClient(Client client);

  void deleteClient(String clientId);
}
