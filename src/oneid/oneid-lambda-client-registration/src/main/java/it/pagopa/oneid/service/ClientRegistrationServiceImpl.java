package it.pagopa.oneid.service;

import com.nimbusds.oauth2.sdk.client.RedirectURIValidator;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.utils.HASHUtils;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.InvalidInputSetException;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.exception.RefreshSecretException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
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

  @Override
  public void patchClientRegistrationDTO(ClientRegistrationDTO source,
      ClientRegistrationDTO target) {
    if (source.getRedirectUris() != null) {
      target.setRedirectUris(source.getRedirectUris());
    }
    if (source.getClientName() != null) {
      target.setClientName(source.getClientName());
    }
    if (source.getDefaultAcrValues() != null) {
      target.setDefaultAcrValues(source.getDefaultAcrValues());
    }
    if (source.getSamlRequestedAttributes() != null) {
      target.setSamlRequestedAttributes(source.getSamlRequestedAttributes());
    }
    if (source.getLogoUri() != null) {
      target.setLogoUri(source.getLogoUri());
    }
    if (source.getPolicyUri() != null) {
      target.setPolicyUri(source.getPolicyUri());
    }
    if (source.getTosUri() != null) {
      target.setTosUri(source.getTosUri());
    }
    if (source.getA11yUri() != null) {
      target.setA11yUri(source.getA11yUri());
    }
    if (source.getLocalizedContentMap() != null) {
      target.setLocalizedContentMap(source.getLocalizedContentMap());
    }
    if (source.getBackButtonEnabled() != null) {
      target.setBackButtonEnabled(source.getBackButtonEnabled());
    }
    if (source.getRequiredSameIdp() != null) {
      target.setRequiredSameIdp(source.getRequiredSameIdp());
    }
    if (source.getSpidMinors() != null) {
      target.setSpidMinors(source.getSpidMinors());
    }
    if (source.getSpidProfessionals() != null) {
      target.setSpidProfessionals(source.getSpidProfessionals());
    }
    if (source.getPairwise() != null) {
      target.setPairwise(source.getPairwise());
    }
  }

  @Override
  public void validateClientRegistrationInfo(
      ClientRegistrationDTO clientRegistrationDTO) {

    // Validate redirectUris
    if (clientRegistrationDTO.getRedirectUris() != null) {
      if (clientRegistrationDTO.getRedirectUris().isEmpty()) {
        throw new InvalidUriException(ClientRegistrationErrorCode.EMPTY_URI);
      }
      for (String redirectUri : clientRegistrationDTO.getRedirectUris()) {
        try {
          CustomURIUtils.validateURI(redirectUri);
          RedirectURIValidator.ensureLegal(URI.create(redirectUri));
        } catch (IllegalArgumentException ex) {
          throw new InvalidUriException(ClientRegistrationErrorCode.INVALID_URI);
        } catch (NullPointerException ex) {
          throw new InvalidUriException(ClientRegistrationErrorCode.EMPTY_URI);
        }
      }
    }

    // Validate policyUri
    String policyUri = clientRegistrationDTO.getPolicyUri();
    try {
      if (!StringUtils.isBlank(policyUri)) {
        CustomURIUtils.validateURI(policyUri);
      }

    } catch (InvalidUriException e) {
      throw new InvalidUriException("Invalid Policy URI");
    }

    // Validate logoUri
    String logoUri = clientRegistrationDTO.getLogoUri();
    try {
      if (!StringUtils.isBlank(logoUri)) {
        CustomURIUtils.validateURI(logoUri);
      }

    } catch (InvalidUriException e) {
      throw new InvalidUriException("Invalid Logo URI");
    }

    // Validate tosUri
    String tosUri = clientRegistrationDTO.getTosUri();
    try {
      if (!StringUtils.isBlank(tosUri)) {
        CustomURIUtils.validateURI(tosUri);
      }

    } catch (InvalidUriException e) {
      throw new InvalidUriException("Invalid TOS URI");
    }

    // Validate a11yUri
    String a11yUri = clientRegistrationDTO.getA11yUri();
    try {
      if (!StringUtils.isBlank(a11yUri)) {
        CustomURIUtils.validateURI(a11yUri);
      }

    } catch (InvalidUriException e) {
      throw new InvalidUriException("Invalid a11y URI");
    }

    // Validate defaultAcrValues
    Set<String> defaultAcrValues = clientRegistrationDTO.getDefaultAcrValues();
    if (defaultAcrValues != null) {
      if (defaultAcrValues.isEmpty() || defaultAcrValues.stream()
          .anyMatch(authLevel -> StringUtils.isBlank(authLevel)
              || AuthLevel.authLevelFromValue(authLevel) == null)) {
        throw new InvalidInputSetException("Invalid default ACR values");
      }
    }

    // Validate samlRequestedAttributes
    Set<Identifier> samlRequestedAttributes = clientRegistrationDTO.getSamlRequestedAttributes();
    if (samlRequestedAttributes != null) {
      if (samlRequestedAttributes.isEmpty()) {
        throw new InvalidInputSetException("No SAML requested attributes provided");
      }
      for (Identifier attribute : samlRequestedAttributes) {
        if (attribute == null) {
          throw new InvalidInputSetException("SAML requested attribute cannot be null");
        }
        try {
          Identifier.valueOf(attribute.name());
        } catch (IllegalArgumentException e) {
          throw new InvalidInputSetException("Invalid SAML requested attribute: " + attribute);
        }
      }
    }
  }

  @Override
  public ClientRegistrationResponseDTO saveClient(
      ClientRegistrationDTO clientRegistrationDTO) {
    // 1. call to dynamo & set in clientRegistrationRequestDTO
    int maxAttributeIndex = findMaxAttributeIndex();

    // 2. Convert ClientRegistrationRequestDto to Client and sets newly generated fields
    Client client = ClientUtils.convertClientRegistrationDTOToClient(clientRegistrationDTO);
    client.setClientId(new ClientID(32).getValue());
    client.setClientIdIssuedAt(System.currentTimeMillis());
    client.setAttributeIndex(maxAttributeIndex + 1);
    Log.debugf("Client converted from ClientRegistrationDTO: %s", client.getClientId());

    // 3. Client.Secret & Salt
    ClientSecretSalt clientSecretSalt = generateClientSecretSalt();

    // 4. Create ClientExtended
    ClientExtended clientExtended = new ClientExtended(client, clientSecretSalt.hashedSecret,
        HASHUtils.b64encoder.encodeToString(clientSecretSalt.salt));

    // 5. Save on Dynamo
    clientConnector.saveClientIfNotExists(clientExtended);
    Log.debugf("Saved client with clientId: %s", clientExtended.getClientId());

    // 6. Overwrite clientRegistrationRequestDTO.defaultAcrValues with only its first value
    clientRegistrationDTO.setDefaultAcrValues(Set.of(
        clientRegistrationDTO.getDefaultAcrValues().stream().findFirst()
            .orElseThrow(ClientRegistrationServiceException::new)));

    // 7. create and return ClientRegistrationResponseDTO
    return ClientRegistrationResponseDTO.builder()
        .userId(clientRegistrationDTO.getUserId())
        .redirectUris(clientRegistrationDTO.getRedirectUris())
        .clientName(clientRegistrationDTO.getClientName())
        .logoUri(clientRegistrationDTO.getLogoUri())
        .policyUri(clientRegistrationDTO.getPolicyUri())
        .tosUri(clientRegistrationDTO.getTosUri())
        .defaultAcrValues(clientRegistrationDTO.getDefaultAcrValues())
        .samlRequestedAttributes(clientRegistrationDTO.getSamlRequestedAttributes())
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
        .clientID(client.getClientId())
        .clientSecret(HASHUtils.b64encoder.encodeToString(clientSecretSalt.secret))
        .clientIdIssuedAt(client.getClientIdIssuedAt())
        .build();
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
  public Client getClient(String clientId, String userId) {
    Client client = clientConnector.getClientById(clientId)
        .orElseThrow(ClientNotFoundException::new);
    return client;
  }

  @Override
  public void updateClientRegistrationDTO(ClientRegistrationDTO clientRegistrationDTO,
      String clientID, int attributeIndex, long clientIdIssuedAt) {
    Client client = ClientUtils.convertClientRegistrationDTOToClient(clientRegistrationDTO);
    client.setClientId(clientID);
    client.setAttributeIndex(attributeIndex);
    client.setClientIdIssuedAt(clientIdIssuedAt);
    clientConnector.updateClient(client);

  }

  @Override
  public String refreshClientSecret(String clientId, String userId) {

    // 1. Get the client from DynamoDB using the retrieved clientId
    Optional<Client> client = clientConnector.getClientById(clientId);
    if (client.isEmpty()) {
      Log.errorf("No client found for clientId: %s", clientId);
      throw new RefreshSecretException("No client found for the clientId associated to this user");
    }

    // 2. Check that the userId is the same as the one passed in the request
    if (!userId.equals(client.get().getUserId())) {
      Log.errorf("UserId mismatch for clientId: %s, expected: %s, found: %s", clientId, userId,
          client.get().getUserId());
      throw new RefreshSecretException("User ID mismatch");
    }

    // 2. Generate a new secret and salt
    ClientSecretSalt newClientSecretSalt = generateClientSecretSalt();
    Log.debugf("Generated new client secret and salt for clientId: %s", clientId);

    // 3. Update the client information in Dynamo
    clientConnector.updateClientSecretSalt(client.get(),
        HASHUtils.b64encoder.encodeToString(newClientSecretSalt.salt),
        newClientSecretSalt.hashedSecret);

    Log.debugf("Updated client secret and salt for clientId: %s", clientId);

    // 4. Return the new secret to the caller
    return HASHUtils.b64encoder.encodeToString(newClientSecretSalt.secret);
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
