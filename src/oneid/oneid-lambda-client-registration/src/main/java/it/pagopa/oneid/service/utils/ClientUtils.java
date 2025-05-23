package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.common.utils.ClientConstants.ACS_INDEX_DEFAULT_VALUE;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.model.dto.ClientMetadataDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLogging
public class ClientUtils {


  public static Client convertClientRegistrationDTOToClient(
      ClientRegistrationRequestDTO clientRegistrationRequestDTO, int maxAttributeIndex) {
    Log.debug("start");

    ClientID clientID = new ClientID(32); //todo length?
    long clientIdIssuedAt = System.currentTimeMillis();

    Set<String> requestedParameters = clientRegistrationRequestDTO.getSamlRequestedAttributes()
        .stream()
        .map(Identifier::name)
        .collect(Collectors.toSet());

    Set<String> callbackUris = clientRegistrationRequestDTO.getRedirectUris();

    return Client.builder()
        .clientId(clientID.getValue())
        .friendlyName(clientRegistrationRequestDTO.getClientName())
        .callbackURI(callbackUris)
        .requestedParameters(requestedParameters)
        .authLevel(AuthLevel.authLevelFromValue(
            clientRegistrationRequestDTO.getDefaultAcrValues().stream().findFirst()
                .orElseThrow(ClientRegistrationServiceException::new)))
        .acsIndex(ACS_INDEX_DEFAULT_VALUE)
        .attributeIndex(maxAttributeIndex + 1)
        .isActive(true)
        .clientIdIssuedAt(clientIdIssuedAt)
        .logoUri(clientRegistrationRequestDTO.getLogoUri())
        .policyUri(clientRegistrationRequestDTO.getPolicyUri())
        .tosUri(clientRegistrationRequestDTO.getTosUri())
        .build();
  }

  public static ClientMetadataDTO convertClientToClientMetadataDTO(Client client) {
    Set<Identifier> samlRequestedAttributes = client
        .getRequestedParameters()
        .stream()
        .map(Identifier::valueOf)
        .collect(Collectors.toSet());

    return ClientMetadataDTO.builder()
        .redirectUris(client
            .getCallbackURI())
        .clientName(client.getFriendlyName())
        .logoUri(Optional.ofNullable(client.getLogoUri()).orElse(""))
        .defaultAcrValues(Set.of(client.getAuthLevel().getValue()))
        .samlRequestedAttributes(samlRequestedAttributes)
        .policyUri(client.getPolicyUri())
        .tosUri(client.getTosUri())
        .build();

  }

}
