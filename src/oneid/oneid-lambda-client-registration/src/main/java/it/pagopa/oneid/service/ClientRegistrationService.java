package it.pagopa.oneid.service;

import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;

public interface ClientRegistrationService {

  void validateClientRegistrationInfo(ClientRegistrationDTO clientRegistrationDTO);

  ClientRegistrationResponseDTO saveClient(
      ClientRegistrationDTO clientRegistrationDTO);

  ClientRegistrationDTO getClientRegistrationDTO(String clientId, String userId);

  void updateClientRegistrationDTO(String clientID, ClientRegistrationDTO clientRegistrationDTO);

  void patchClientRegistrationDTO(ClientRegistrationDTO source,
      ClientRegistrationDTO target);

  String refreshClientSecret(String clientId, String userId);

}
