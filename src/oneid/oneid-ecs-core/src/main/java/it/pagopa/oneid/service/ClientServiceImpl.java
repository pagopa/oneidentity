package it.pagopa.oneid.service;

import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientFE;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;

@ApplicationScoped
@CustomLogging
public class ClientServiceImpl implements ClientService {

  @Inject
  ClientConnectorImpl clientConnectorImpl;

  @Override
  public Optional<ClientFE> getClientInformation(@NotBlank String clientID) {
    Optional<Client> client = clientConnectorImpl.getClientById(clientID);
    return client.map(ClientFE::new);
  }
}
