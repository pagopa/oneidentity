package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.dto.PDVApiKeysDTO;
import it.pagopa.oneid.common.model.dto.PDVValidateApiKeyDTO;
import it.pagopa.oneid.common.model.dto.PDVValidationResponseDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import javax.annotation.Nullable;

public interface ClientRegistrationService {

  void validateClientRegistrationInfo(ClientRegistrationDTO clientRegistrationDTO);

  ClientRegistrationResponseDTO saveClient(
      ClientRegistrationDTO clientRegistrationDTO, String userId, @Nullable String pdvApiKey);

  Client getClientExtendedByClientId(String clientId);

  Client getClientByUserId(String userId);

  void updateClientExtended(ClientRegistrationDTO clientRegistrationDTO,
      ClientExtended clientExtended, @Nullable String pdvApiKey);

  String refreshClientSecret(String clientId, String userId);

  PDVApiKeysDTO getPDVPlanList();

  PDVValidationResponseDTO validatePDVApiKey(PDVValidateApiKeyDTO validateApiKeyDTO);
}
