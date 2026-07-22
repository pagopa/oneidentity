package it.pagopa.oneid.service;

import io.quarkus.runtime.Startup;
import it.pagopa.oneid.common.connector.ClientConnector;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientFE;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@CustomLogging
@Startup
public class ClientServiceImpl implements ClientService {

  private final ClientConnector clientConnector;

  @Inject
  ClientServiceImpl(ClientConnector clientConnector) {
    this.clientConnector = clientConnector;
  }

  @Override
  public Optional<ArrayList<ClientFE>> getAllClientsInformation() {
    Optional<ArrayList<Client>> clients = clientConnector.findAllActive();
    return clients.map(cl ->
        cl.stream()
            .map(ClientFE::new)
            .collect(Collectors.toCollection(ArrayList::new)
            )
    );
  }
}
