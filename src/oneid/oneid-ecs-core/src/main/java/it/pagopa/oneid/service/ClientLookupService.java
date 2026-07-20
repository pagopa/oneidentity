package it.pagopa.oneid.service;

import java.util.Optional;
import it.pagopa.oneid.common.model.Client;


public interface ClientLookupService {
  Optional<Client> getClientById(String clientId);
}
