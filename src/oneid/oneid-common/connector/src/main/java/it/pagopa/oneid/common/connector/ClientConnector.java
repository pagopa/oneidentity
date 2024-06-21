package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.Client;
import java.util.ArrayList;
import java.util.Optional;

public interface ClientConnector {

  Optional<ArrayList<Client>> findAll();
}
