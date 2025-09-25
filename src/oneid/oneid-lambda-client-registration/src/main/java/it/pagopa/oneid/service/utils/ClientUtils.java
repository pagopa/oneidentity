package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.common.utils.ClientConstants.ACS_INDEX_DEFAULT_VALUE;
import com.nimbusds.jwt.SignedJWT;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.InvalidBearerTokenException;
import it.pagopa.oneid.exception.UserIdMismatchException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import java.text.ParseException;
import java.util.Optional;
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
      ClientRegistrationDTO clientRegistrationDTO, String userId) {
    Log.debug("start");

    Set<String> requestedParameters = clientRegistrationDTO.getSamlRequestedAttributes();

    Set<String> callbackUris = clientRegistrationDTO.getRedirectUris();

    //clientID, attributeIndex and clientIdIssuedAt are set outside this method
    return Client.builder()
        .userId(userId)
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
        .requiredSameIdp(clientRegistrationDTO.getRequiredSameIdp() != null
            ? clientRegistrationDTO.getRequiredSameIdp() : false)
        .build();
  }

  public static ClientRegistrationDTO convertClientToClientRegistrationDTO(Client client) {

    return ClientRegistrationDTO.builder()
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

  public static Optional<String> getUpdateMessage(ClientRegistrationDTO input,
      Client existingClient) {
    String message = "";
    if (!input.getClientName().equals(existingClient.getFriendlyName())) {
      message += "ClientName; ";
    }
    if (!input.getSamlRequestedAttributes().equals(existingClient.getRequestedParameters())) {
      message += "SamlRequestedAttributes; ";
    }
    AuthLevel authLevel = AuthLevel.authLevelFromValue(
        input.getDefaultAcrValues().stream().findFirst().get());
    if (!authLevel.equals(existingClient.getAuthLevel())) {
      message += "DefaultAcrValues; ";
    }
    if (StringUtils.isNotBlank(message)) {
      return Optional.of(message);
    }
    return Optional.empty();
  }

  public static String getUseridFromBearer(String bearer) {
    if (bearer == null || !bearer.startsWith("Bearer ")) {
      throw new InvalidBearerTokenException("Invalid or missing Header Authorization");
    }
    String token = bearer.substring("Bearer ".length());
    String userId = null;
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      userId = signedJWT.getJWTClaimsSet().getSubject();
    } catch (ParseException e) {
      throw new InvalidBearerTokenException("Error parsing bearer token of Header Authorization");
    }
    if (userId == null || userId.isEmpty()) {
      throw new InvalidBearerTokenException("Missing subject in bearer token");
    }
    return userId;
  }
}
