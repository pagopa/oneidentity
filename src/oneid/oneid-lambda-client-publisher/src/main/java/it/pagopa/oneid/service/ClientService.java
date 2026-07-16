package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.ClientFE;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Optional;

public interface ClientService {
  Optional<ArrayList<ClientFE>> getAllClientsInformation();
}
