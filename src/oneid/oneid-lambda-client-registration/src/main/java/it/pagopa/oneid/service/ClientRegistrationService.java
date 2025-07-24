package it.pagopa.oneid.service;

import it.pagopa.oneid.model.dto.ClientMetadataDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;

public interface ClientRegistrationService {

  void validateClientRegistrationInfo(ClientRegistrationRequestDTO clientRegistrationRequestDTO);

  ClientRegistrationResponseDTO saveClient(
      ClientRegistrationRequestDTO clientRegistrationRequestDTO);

  ClientMetadataDTO getClientMetadataDTO(String clientId);

  String refreshClientSecret(String userId);

}
