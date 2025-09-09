package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;

public interface ClientRegistrationService {

  void validateClientRegistrationInfo(ClientRegistrationDTO clientRegistrationDTO);

  ClientRegistrationResponseDTO saveClient(
      ClientRegistrationDTO clientRegistrationDTO);

  Client getClientByClientId(String clientId);

  Client getClientByUserId(String userId);

  void updateClientRegistrationDTO(ClientRegistrationDTO clientRegistrationDTO, String clientId,
      int attributeIndex, long clientIdIssuedAt);

  String refreshClientSecret(String clientId, String userId);

}
