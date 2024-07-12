package it.pagopa.oneid.service;

import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

  @Override
  public void validateClientRegistrationInfo(
      ClientRegistrationRequestDTO clientRegistrationRequestDTO) {
    //TODO implement
  }

  @Override
  public void saveClient(ClientRegistrationRequestDTO clientRegistrationRequestDTO) {
    //TODO implement
  }

}
