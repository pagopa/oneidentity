package it.pagopa.oneid.service;

import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;

public interface ClientRegistrationService {

  void validateClientRegistrationInfo(ClientRegistrationRequestDTO clientRegistrationRequestDTO);

  void saveClient(ClientRegistrationRequestDTO clientRegistrationRequestDTO);

}
