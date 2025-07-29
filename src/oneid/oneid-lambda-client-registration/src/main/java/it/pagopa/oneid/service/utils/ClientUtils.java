package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.common.utils.ClientConstants.ACS_INDEX_DEFAULT_VALUE;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLogging
public class ClientUtils {


  public static Client convertClientRegistrationDTOToClient(
      ClientRegistrationDTO clientRegistrationDTO, int attributeIndex) {
    Log.debug("start");

    ClientID clientID = new ClientID(32); //todo length?
    long clientIdIssuedAt = System.currentTimeMillis();

    Set<String> requestedParameters = clientRegistrationDTO.getSamlRequestedAttributes()
        .stream()
        .map(Identifier::name)
        .collect(Collectors.toSet());

    Set<String> callbackUris = clientRegistrationDTO.getRedirectUris();

    return Client.builder()
        .userId(clientRegistrationDTO.getUserId())
        .clientId(clientID.getValue())
        .friendlyName(clientRegistrationDTO.getClientName())
        .callbackURI(callbackUris)
        .requestedParameters(requestedParameters)
        .authLevel(AuthLevel.authLevelFromValue(
            clientRegistrationDTO.getDefaultAcrValues().stream().findFirst()
                .orElseThrow(ClientRegistrationServiceException::new)))
        .acsIndex(ACS_INDEX_DEFAULT_VALUE)
        .attributeIndex(attributeIndex)
        .isActive(true)
        .clientIdIssuedAt(clientIdIssuedAt)
        .logoUri(clientRegistrationDTO.getLogoUri())
        .policyUri(clientRegistrationDTO.getPolicyUri())
        .tosUri(clientRegistrationDTO.getTosUri())
        .a11yUri(clientRegistrationDTO.getA11yUri())
        .backButtonEnabled(clientRegistrationDTO.isBackButtonEnabled())
        .localizedContentMap(clientRegistrationDTO.getLocalizedContentMap())
        .spidMinors(clientRegistrationDTO.isSpidMinors())
        .spidProfessionals(clientRegistrationDTO.isSpidProfessionals())
        .pairwise(clientRegistrationDTO.isPairwise())
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
        .logoUri(Optional.ofNullable(client.getLogoUri()).orElse(""))
        .policyUri(client.getPolicyUri())
        .a11yUri(client.getA11yUri())
        .backButtonEnabled(client.isBackButtonEnabled())
        .localizedContentMap(client.getLocalizedContentMap())
        .build();

  }

}
