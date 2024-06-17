package it.pagopa.oneid.connector;

import it.pagopa.oneid.common.Client;
import java.util.ArrayList;
import java.util.Optional;

public interface ClientConnector {

  Optional<ArrayList<Client>> findAll();
}
