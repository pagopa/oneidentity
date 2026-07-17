package it.pagopa.oneid.service;

import io.quarkus.runtime.Startup;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientFE;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@CustomLogging
@Startup
public class ClientServiceImpl implements ClientService {

  @Inject
  ClientConnectorImpl clientConnectorImpl;

  @Override
  public Optional<ClientFE> getClientInformation(@NotBlank String clientID) {
    Optional<Client> client = clientConnectorImpl.getClientById(clientID);
    return client.map(ClientFE::new);
  }

  @Override
  public Optional<ArrayList<ClientFE>> getAllClientsInformation() {
    Optional<ArrayList<Client>> clients = clientConnectorImpl.findAll();
    return clients.map(cl ->
        cl.stream()
            .map(ClientFE::new)
            .collect(Collectors.toCollection(ArrayList::new)
            )
    );
  }
}
