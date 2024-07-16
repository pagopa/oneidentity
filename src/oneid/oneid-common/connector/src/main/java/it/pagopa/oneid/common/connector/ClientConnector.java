package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.dto.SecretDTO;
import java.util.ArrayList;
import java.util.Optional;

public interface ClientConnector {

  Optional<ArrayList<Client>> findAll();

  Optional<SecretDTO> getClientSecret(String clientId);

  //TODO add Exception
  void saveClientIfNotExists(Client client);
}
