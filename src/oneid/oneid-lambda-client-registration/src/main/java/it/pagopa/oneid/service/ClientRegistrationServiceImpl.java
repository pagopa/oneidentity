package it.pagopa.oneid.service;

import static it.pagopa.oneid.service.utils.ClientUtils.convertClientToClientMetadataDTO;
import com.nimbusds.oauth2.sdk.client.RedirectURIValidator;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.utils.HASHUtils;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.connector.CognitoConnectorImpl;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.exception.RefreshSecretException;
import it.pagopa.oneid.model.dto.ClientMetadataDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.model.enums.ClientRegistrationErrorCode;
import it.pagopa.oneid.service.utils.ClientUtils;
import it.pagopa.oneid.service.utils.CustomURIUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
@CustomLogging
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

  @Inject
  ClientConnectorImpl clientConnector;
  @Inject
  CognitoConnectorImpl cognitoConnector;

  @Override
  public void validateClientRegistrationInfo(
      ClientRegistrationRequestDTO clientRegistrationRequestDTO) {

    // Validate redirectUris
    for (String redirectUri : clientRegistrationRequestDTO.getRedirectUris()) {
      try {
        CustomURIUtils.validateURI(redirectUri);
        RedirectURIValidator.ensureLegal(URI.create(redirectUri));
      } catch (IllegalArgumentException ex) {
        throw new InvalidUriException(ClientRegistrationErrorCode.INVALID_REDIRECT_URI);
      } catch (NullPointerException ex) {
        throw new InvalidUriException(ClientRegistrationErrorCode.REDIRECT_URI_NULL);
      }
    }

    // Validate policyUri
    String policyUri = clientRegistrationRequestDTO.getPolicyUri();
    try {
      if (!StringUtils.isBlank(policyUri)) {
        CustomURIUtils.validateURI(policyUri);
      }

    } catch (InvalidUriException e) {
      throw new InvalidUriException("Invalid Policy URI");
    }

    // Validate logoUri
    String logoUri = clientRegistrationRequestDTO.getLogoUri();
    try {
      if (!StringUtils.isBlank(logoUri)) {
        CustomURIUtils.validateURI(logoUri);
      }

    } catch (InvalidUriException e) {
      throw new InvalidUriException("Invalid Logo URI");
    }

    // Validate tosUri
    String tosUri = clientRegistrationRequestDTO.getTosUri();
    try {
      if (!StringUtils.isBlank(tosUri)) {
        CustomURIUtils.validateURI(tosUri);
      }

    } catch (InvalidUriException e) {
      throw new InvalidUriException("Invalid TOS URI");
    }


  }

  @Override
  public ClientRegistrationResponseDTO saveClient(
      ClientRegistrationRequestDTO clientRegistrationRequestDTO) {
    // 1. call to dynamo & set in clientRegistrationRequestDTO
    int maxAttributeIndex = findMaxAttributeIndex();

    // 2. Convert ClientRegistrationRequestDto -> Client
    Client client = ClientUtils.convertClientRegistrationDTOToClient(clientRegistrationRequestDTO,
        maxAttributeIndex);

    // 3. Client.Secret & Salt

    ClientSecretSalt clientSecretSalt = generateClientSecretSalt();

    // 4. Create ClientExtended
    ClientExtended clientExtended = new ClientExtended(client, clientSecretSalt.hashedSecret,
        HASHUtils.b64encoder.encodeToString(clientSecretSalt.salt));

    // 5. Save on Dynamo
    clientConnector.saveClientIfNotExists(clientExtended);

    // 6. Overwrite clientRegistrationRequestDTO.defaultAcrValues with only its first value
    clientRegistrationRequestDTO.setDefaultAcrValues(Set.of(
        clientRegistrationRequestDTO.getDefaultAcrValues().stream().findFirst()
            .orElseThrow(ClientRegistrationServiceException::new)));

    // 7. create and return ClientRegistrationResponseDTO
    return new ClientRegistrationResponseDTO(clientRegistrationRequestDTO,
        client.getClientId(), HASHUtils.b64encoder.encodeToString(clientSecretSalt.secret),
        client.getClientIdIssuedAt());
  }


  private int findMaxAttributeIndex() {
    return clientConnector
        .findAll()
        .map(clients -> clients
            .stream()
            .max(Comparator.comparing(Client::getAttributeIndex))
            .map(Client::getAttributeIndex)
            .orElse(-1)
        ).orElse(-1);
  }


  @Override
  public ClientMetadataDTO getClientMetadataDTO(String clientId) {
    Client client = clientConnector.getClientById(clientId)
        .orElseThrow(ClientNotFoundException::new);

    return convertClientToClientMetadataDTO(client);
  }


  @Override
  public String refreshClientSecret(String userId) {

    // 1. Use the userId to get the clientId from Cognito
    Optional<String> clientId = cognitoConnector.extractClientIdByUserId(userId);
    if (clientId.isEmpty()) {
      Log.errorf("No clientId found for userId: %s", userId);
      throw new RefreshSecretException("No clientId associated to this user");
    }
    // 2. Get the client from DynamoDB using the retrieved clientId
    String clientIdValue = clientId.get();
    Optional<Client> client = clientConnector.getClientById(clientIdValue);
    if (client.isEmpty()) {
      Log.errorf("No client found for clientId: %s", clientId.get());
      throw new RefreshSecretException("No client found for the clientId associated to this user");
    }

    // 3. Generate a new secret and salt
    ClientSecretSalt newClientSecretSalt = generateClientSecretSalt();
    Log.debugf("Generated new client secret and salt for clientId: %s", clientIdValue);

    //4. Update the client information in Dynamo
    clientConnector.updateClientSecretSalt(client.get(),
        HASHUtils.b64encoder.encodeToString(newClientSecretSalt.salt),
        newClientSecretSalt.hashedSecret);

    Log.debugf("Updated client secret and salt for clientId: %s", clientIdValue);

    // 5. Return the new secret to the caller
    return newClientSecretSalt.hashedSecret;
  }

  private ClientSecretSalt generateClientSecretSalt() {
    // a. Generate Salt
    byte[] salt = HASHUtils.generateSecureRandom(16);

    // b. Generate client_secret
    byte[] secret = HASHUtils.generateSecureRandom(32);

    // c. Generate Argon2 of secret using salt
    String hashedClientSecret = HASHUtils.generateArgon2(salt, secret);

    return new ClientSecretSalt(secret, salt, hashedClientSecret);
  }

  private record ClientSecretSalt(byte[] secret, byte[] salt, String hashedSecret) {

  }
}
