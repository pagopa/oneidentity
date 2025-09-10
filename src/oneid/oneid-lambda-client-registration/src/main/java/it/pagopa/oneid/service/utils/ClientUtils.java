package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.common.utils.ClientConstants.ACS_INDEX_DEFAULT_VALUE;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.exception.UserIdMismatchException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

@CustomLogging
public class ClientUtils {


  public static void checkUserId(String inputUserId, String existingUserId) {
    // Check if the userId matches the client userId on db
    if (!StringUtils.equals(inputUserId, existingUserId)) {
      Log.errorf("userId in input %s does not match existing userId on db %s",
          inputUserId, existingUserId);
      throw new UserIdMismatchException();
    }
  }

  public static Client convertClientRegistrationDTOToClient(
      ClientRegistrationDTO clientRegistrationDTO) {
    Log.debug("start");

    Set<String> requestedParameters =
        clientRegistrationDTO.getSamlRequestedAttributes() != null
            ? clientRegistrationDTO.getSamlRequestedAttributes()
            : Collections.emptySet();

    Set<String> callbackUris =
        clientRegistrationDTO.getRedirectUris() != null
            ? clientRegistrationDTO.getRedirectUris()
            : Collections.emptySet();

    Set<String> acrValues = clientRegistrationDTO.getDefaultAcrValues();
    AuthLevel authLevel = (acrValues == null || acrValues.isEmpty())
        ? null
        : AuthLevel.authLevelFromValue(acrValues.iterator().next());

    //clientID, attributeIndex and clientIdIssuedAt are set outside this method
    return Client.builder()
        .userId(clientRegistrationDTO.getUserId())
        .friendlyName(clientRegistrationDTO.getClientName())
        .callbackURI(callbackUris)
        .requestedParameters(requestedParameters)
        .authLevel(authLevel)
        .acsIndex(ACS_INDEX_DEFAULT_VALUE)
        .isActive(true)
        .logoUri(clientRegistrationDTO.getLogoUri())
        .policyUri(clientRegistrationDTO.getPolicyUri())
        .tosUri(clientRegistrationDTO.getTosUri())
        .a11yUri(clientRegistrationDTO.getA11yUri())
        .localizedContentMap(clientRegistrationDTO.getLocalizedContentMap())
        .backButtonEnabled(clientRegistrationDTO.getBackButtonEnabled() != null
            ? clientRegistrationDTO.getBackButtonEnabled() : false)
        .spidMinors(clientRegistrationDTO.getSpidMinors() != null
            ? clientRegistrationDTO.getSpidMinors() : false)
        .spidProfessionals(clientRegistrationDTO.getSpidProfessionals() != null
            ? clientRegistrationDTO.getSpidProfessionals() : false)
        .pairwise(clientRegistrationDTO.getPairwise() != null
            ? clientRegistrationDTO.getPairwise() : false)
        .requiredSameIdp(clientRegistrationDTO.getRequiredSameIdp() != null
            ? clientRegistrationDTO.getRequiredSameIdp() : false)
        .build();
  }

  public static ClientRegistrationDTO convertClientToClientRegistrationDTO(Client client) {

    return ClientRegistrationDTO.builder()
        .userId(client.getUserId())
        .redirectUris(client.getCallbackURI())
        .clientName(client.getFriendlyName())
        .defaultAcrValues(Set.of(client.getAuthLevel().getValue()))
        .samlRequestedAttributes(client.getRequestedParameters())
        .requiredSameIdp(client.isRequiredSameIdp())
        .spidMinors(client.isSpidMinors())
        .spidProfessionals(client.isSpidProfessionals())
        .pairwise(client.isPairwise())
        .tosUri(client.getTosUri())
        .logoUri(client.getLogoUri())
        .policyUri(client.getPolicyUri())
        .a11yUri(client.getA11yUri())
        .backButtonEnabled(client.isBackButtonEnabled())
        .localizedContentMap(client.getLocalizedContentMap())
        .build();

  }


}
