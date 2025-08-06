package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.common.utils.ClientConstants.ACS_INDEX_DEFAULT_VALUE;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.LocalizedContent;
import it.pagopa.oneid.common.model.LocalizedContentMap;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.InvalidLocalizedContentMapException;
import it.pagopa.oneid.exception.UserIdMismatchException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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

    Set<String> requestedParameters = clientRegistrationDTO.getSamlRequestedAttributes()
        .stream()
        .map(Identifier::name)
        .collect(Collectors.toSet());

    Set<String> callbackUris = clientRegistrationDTO.getRedirectUris();

    //clientID, attributeIndex and clientIdIssuedAt are set outside this method
    return Client.builder()
        .userId(clientRegistrationDTO.getUserId())
        .friendlyName(clientRegistrationDTO.getClientName())
        .callbackURI(callbackUris)
        .requestedParameters(requestedParameters)
        .authLevel(AuthLevel.authLevelFromValue(
            clientRegistrationDTO.getDefaultAcrValues().stream().findFirst()
                .orElseThrow(ClientRegistrationServiceException::new)))
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
        .build();
  }

  public static ClientRegistrationDTO convertClientToClientRegistrationDTO(Client client) {
    Set<Identifier> samlRequestedAttributes = client
        .getRequestedParameters()
        .stream()
        .map(Identifier::valueOf)
        .collect(Collectors.toSet());

    return ClientRegistrationDTO.builder()
        .userId(client.getUserId())
        .redirectUris(client.getCallbackURI())
        .clientName(client.getFriendlyName())
        .defaultAcrValues(Set.of(client.getAuthLevel().getValue()))
        .samlRequestedAttributes(samlRequestedAttributes)
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

  public static void validateLocalizedContentMap(LocalizedContentMap localizedContentMap) {
    if (localizedContentMap != null) {
      Map<String, Map<String, LocalizedContent>> contentMap = localizedContentMap.getContentMap();
      
      if (contentMap == null || contentMap.isEmpty()) {
        throw new InvalidLocalizedContentMapException(
            "If localizedContentMap is provided, its contentMap must not be empty");
      }

      // collect all non-null content entries into a single list
      List<LocalizedContent> localizedContentList = contentMap.values().stream()
          .flatMap(innerMap -> innerMap.values().stream())
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      // Check if any actual content was found after flattening
      if (localizedContentList.isEmpty()) {
        throw new InvalidLocalizedContentMapException(
            "If localizedContentMap is provided, it must contain at least one content entry");
      }

      // Check if any of the found entries have missing fields
      if (localizedContentList.stream().anyMatch(content ->
          StringUtils.isBlank(content.getTitle())
              || StringUtils.isBlank(content.getDescription())
              || StringUtils.isBlank(content.getDocUri())
              || StringUtils.isBlank(content.getSupportAddress())
              || StringUtils.isBlank(content.getCookieUri())
      )) {
        throw new InvalidLocalizedContentMapException(
            "All provided localized content entries must have all required fields");
      }

    }
  }


}
