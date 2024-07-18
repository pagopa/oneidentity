package it.pagopa.oneid.service;

import com.nimbusds.oauth2.sdk.id.ClientID;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.utils.HASHUtils;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.service.utils.ClientUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

@ApplicationScoped
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

  @Inject
  ClientConnectorImpl clientConnector;

  @Override
  public void validateClientRegistrationInfo(
      ClientRegistrationRequestDTO clientRegistrationRequestDTO) {
    //TODO implement
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

    // 6. create and return ClientRegistrationResponseDTO
    Log.debug("end");
    return new ClientRegistrationResponseDTO(clientRegistrationRequestDTO,
        new ClientID(client.getClientId()), clientSecret,
        client.getClientIdIssuedAt());
  }

  
  private int findMaxAttributeIndex() {
    //TODO fix .get()
    Log.debug("start");
    return clientConnector.findAll()
        .get()
        .stream()
        .max(Comparator.comparing(Client::getAttributeIndex))
        .map(Client::getAttributeIndex)
        .orElse(0)
        ;
  }
}
