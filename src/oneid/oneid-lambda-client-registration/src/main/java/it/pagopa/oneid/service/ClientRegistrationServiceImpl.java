package it.pagopa.oneid.service;

import com.nimbusds.oauth2.sdk.client.RedirectURIValidator;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.utils.HASHUtils;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.InvalidRedirectURIException;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.model.enums.ClientRegistrationErrorCode;
import it.pagopa.oneid.service.utils.ClientUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

  @Inject
  ClientConnectorImpl clientConnector;

  @Override
  public void validateClientRegistrationInfo(
      ClientRegistrationRequestDTO clientRegistrationRequestDTO) {

    // Validate redirectUris
    for (String uri : clientRegistrationRequestDTO.getRedirectUris()) {
      try {
        RedirectURIValidator.ensureLegal(URI.create(uri));
      } catch (IllegalArgumentException ex) {
        throw new InvalidRedirectURIException(ClientRegistrationErrorCode.INVALID_REDIRECT_URI);
      } catch (NullPointerException ex) {
        throw new InvalidRedirectURIException(ClientRegistrationErrorCode.REDIRECT_URI_NULL);
      }
    }
  }

  @Override
  public ClientRegistrationResponseDTO saveClient(
      ClientRegistrationRequestDTO clientRegistrationRequestDTO) {
    Log.debug("start");
    // 1. call to dynamo & set in clientRegistrationRequestDTO
    int maxAttributeIndex = findMaxAttributeIndex();

    // 2. Convert ClientRegistrationRequestDto -> Client
    Client client = ClientUtils.convertClientRegistrationDTOToClient(clientRegistrationRequestDTO,
        maxAttributeIndex);

    // 3. Client.Secret & Salt

    // a. Generate random string
    String randomString = HASHUtils.generateRandomString(32); //todo length?

    // b. sha256 of random string -> salt
    String salt = HASHUtils.hashSalt(randomString);

    // c. generate client_secret
    String clientSecret;
    try {
      clientSecret = ClientUtils.generateClientSecret();
    } catch (NoSuchAlgorithmException ex) {
      throw new ClientRegistrationServiceException();
    }

    // d. encrypt of salt with client_secret as key
    String encryptedSalt = ClientUtils.encryptSalt(salt, clientSecret);

    // 4. Create ClientExtended
    ClientExtended clientExtended = new ClientExtended(client, encryptedSalt,
        salt);

    // 5. Save on Dynamo
    clientConnector.saveClientIfNotExists(clientExtended);

    // 6. Overwrite clientRegistrationRequestDTO.defaultAcrValues with only its first value
    clientRegistrationRequestDTO.setDefaultAcrValues(List.of(
        clientRegistrationRequestDTO.getDefaultAcrValues().getFirst()));

    // 7. create and return ClientRegistrationResponseDTO
    Log.debug("end");
    return new ClientRegistrationResponseDTO(clientRegistrationRequestDTO,
        new ClientID(client.getClientId()), clientSecret,
        client.getClientIdIssuedAt());
  }


  private int findMaxAttributeIndex() {
    Log.debug("start");
    return clientConnector
        .findAll()
        .map(clients -> clients
            .stream()
            .max(Comparator.comparing(Client::getAttributeIndex))
            .map(Client::getAttributeIndex)
            .orElse(-1)
        ).orElse(-1);

  }
}
