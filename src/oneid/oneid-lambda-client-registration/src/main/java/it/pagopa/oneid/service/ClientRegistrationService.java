package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;

public interface ClientRegistrationService {

  void validateClientRegistrationInfo(ClientRegistrationDTO clientRegistrationDTO);

  ClientRegistrationResponseDTO saveClient(
      ClientRegistrationDTO clientRegistrationDTO);

  Client getClient(String clientId, String userId);

  void updateClientRegistrationDTO(ClientRegistrationDTO clientRegistrationDTO, String clientID,
      int attributeIndex, long clientIdIssuedAt);

  void patchClientRegistrationDTO(ClientRegistrationDTO source,
      ClientRegistrationDTO target);

  String refreshClientSecret(String clientId, String userId);

}
