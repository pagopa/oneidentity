package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.dto.SecretDTO;
import java.util.ArrayList;
import java.util.Optional;

public interface ClientConnector {

  Optional<ArrayList<Client>> findAll();

  Optional<SecretDTO> getClientSecret(String clientId);

  void saveClientIfNotExists(ClientExtended client);

  Optional<Client> getClientById(String clientId);

  Optional<ClientExtended> getClientExtendedById(String clientId);

  Optional<Client> getClientByAttributeConsumingServiceIndex(int attributeConsumingServiceIndex);

  void updateClientExtended(ClientExtended clientExtended);

  void updateClientSecretSalt(Client client, String newSalt, String newHashedSecret);

  Optional<Client> getClientByUserId(String userId);
}
