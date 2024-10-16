package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientFE;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;

@ApplicationScoped
public class ClientServiceImpl implements ClientService {

  @Inject
  ClientConnectorImpl clientConnectorImpl;

  @Override
  public Optional<ClientFE> getClientInformation(@NotBlank String clientID) {
    Log.debug("start");
    Optional<Client> client = clientConnectorImpl.getClientById(clientID);
    return client.map(ClientFE::new);
  }
}
