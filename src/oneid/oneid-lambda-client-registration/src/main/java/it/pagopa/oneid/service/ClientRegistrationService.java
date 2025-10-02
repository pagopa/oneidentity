package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;

public interface ClientRegistrationService {

  void validateClientRegistrationInfo(ClientRegistrationDTO clientRegistrationDTO);

  ClientRegistrationResponseDTO saveClient(
      ClientRegistrationDTO clientRegistrationDTO);

  Client getClientExtendedByClientId(String clientId);

  Client getClientByUserId(String userId);

  void updateClientExtended(ClientRegistrationDTO clientRegistrationDTO,
      ClientExtended clientExtended);

  String refreshClientSecret(String clientId, String userId);

}
