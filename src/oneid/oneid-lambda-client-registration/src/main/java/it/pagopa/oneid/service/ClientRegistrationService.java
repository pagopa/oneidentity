package it.pagopa.oneid.service;

import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;

public interface ClientRegistrationService {

  void validateClientRegistrationInfo(ClientRegistrationDTO clientRegistrationDTO);

  ClientRegistrationResponseDTO saveClient(
      ClientRegistrationDTO clientRegistrationDTO);

  ClientRegistrationDTO getClientRegistrationDTO(String clientId, String userId);

  void clientExists(String clientId);

  ClientRegistrationResponseDTO updateClient(ClientRegistrationDTO clientRegistrationDTO);

  String refreshClientSecret(String clientId, String userId);

}
