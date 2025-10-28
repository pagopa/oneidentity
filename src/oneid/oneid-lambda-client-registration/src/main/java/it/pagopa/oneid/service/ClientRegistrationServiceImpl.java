package it.pagopa.oneid.service;

import static com.nimbusds.oauth2.sdk.util.StringUtils.isBlank;
import com.nimbusds.oauth2.sdk.client.RedirectURIValidator;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.connector.PDVApiPlanClient;
import it.pagopa.oneid.common.connector.exception.NoMasterKeyException;
import it.pagopa.oneid.common.connector.exception.PDVException;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.dto.PDVApiKeysDTO;
import it.pagopa.oneid.common.model.dto.PDVValidateApiKeyDTO;
import it.pagopa.oneid.common.model.dto.PDVValidationResponseDTO;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.model.exception.ExistingUserIdException;
import it.pagopa.oneid.common.utils.HASHUtils;
import it.pagopa.oneid.common.utils.SSMConnectorUtilsImpl;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.InvalidPDVPlanException;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.exception.RefreshSecretException;
import it.pagopa.oneid.exception.SSMUpsertPDVException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.model.enums.ClientRegistrationErrorCode;
import it.pagopa.oneid.service.utils.ClientUtils;
import it.pagopa.oneid.service.utils.CustomURIUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.net.URI;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@CustomLogging
public class ClientRegistrationServiceImpl implements ClientRegistrationService {

  private static final String PDV_API_KEY_PREFIX = "/apikey-pdv/plan-details";
  private static final String PDV_API_CLIENT_KEY_PREFIX = "/pdv/";

  @Inject
  ClientConnectorImpl clientConnector;

  @Inject
  @RestClient
  PDVApiPlanClient pdvApiClient;
  @Inject
  SSMConnectorUtilsImpl ssmConnectorUtilsImpl;

  @Override
  public void validateClientRegistrationInfo(
      ClientRegistrationDTO clientRegistrationDTO, @Nullable String pdvApiKey,
      @Nullable String planName) {

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

    // Validate pairWise
    Boolean pairWise = clientRegistrationDTO.getPairwise();
    if (Boolean.TRUE.equals(pairWise)) {
      if (isBlank(pdvApiKey)) {
        throw new InvalidPDVPlanException("PDV api key empty");
      }
      if (isBlank(planName)) {
        throw new InvalidPDVPlanException("PDV plan name empty");
      }

      // build PDVValidate
      PDVValidateApiKeyDTO req = PDVValidateApiKeyDTO.builder()
          .apiKeyValue(pdvApiKey)
          .apiKeyId(planName)
          .build();
      try {
        PDVValidationResponseDTO res = validatePDVApiKey(req);

        if (res == null || !res.isValid()) {
          throw new InvalidPDVPlanException("PDV validation failed for api key and plan");
        }

        Log.info("PDV validation succeeded for plan=" + planName);

      } catch (NoMasterKeyException e) {
        Log.warn("Missing PDV master key in SSM: " + e.getMessage());
        throw new InvalidPDVPlanException("Internal PDV configuration error, missing master key");
      }
    }
  }


  @Override
  public ClientRegistrationResponseDTO saveClient(
      ClientRegistrationDTO clientRegistrationDTO, String userId, @Nullable String pdvApiKey,
      @Nullable String planName) {
    // 1. call to dynamo & set in clientRegistrationDTO
    int maxAttributeIndex = findMaxAttributeIndex();

    // 2. Convert ClientRegistrationDTO to Client and sets newly generated fields
    Client client = ClientUtils.convertClientRegistrationDTOToClient(clientRegistrationDTO, userId);
    client.setClientId(new ClientID(32).getValue());
    client.setClientIdIssuedAt(System.currentTimeMillis());
    client.setAttributeIndex(maxAttributeIndex + 1);
    Log.debugf("Client converted from ClientRegistrationDTO: %s", client.getClientId());

    // 3. Client.Secret & Salt
    ClientSecretSalt clientSecretSalt = generateClientSecretSalt();

    // 4. Create ClientExtended
    ClientExtended clientExtended = new ClientExtended(client, clientSecretSalt.hashedSecret,
        HASHUtils.b64encoder.encodeToString(clientSecretSalt.salt));

    // 5. Check if userId doesn't exist
    if (!clientConnector.getClientByUserId(clientExtended.getUserId()).isEmpty()) {
      Log.errorf("Client with userId %s already exists", clientExtended.getUserId());
      throw new ExistingUserIdException("UserId already exists: " + clientExtended.getUserId());
    }

    // 6. update SSM Pdv parameter
    if (client.isPairwise()) {
      String ssmPath = PDV_API_CLIENT_KEY_PREFIX + clientExtended.getClientId();
      if (!ssmConnectorUtilsImpl.upsertSecureStringIfPresentOnlyIfChanged(ssmPath, pdvApiKey)) {
        Log.errorf("SSM upsert failed %s", ssmPath);
        throw new SSMUpsertPDVException("SSM upsert failed %s: ", ssmPath);
      }
    }

    // 7. Save client on db
    clientConnector.saveClientIfNotExists(clientExtended);
    Log.debugf("Saved client with clientId: %s", clientExtended.getClientId());

    // 8. Overwrite clientRegistrationRequestDTO.defaultAcrValues with only its first value
    clientRegistrationDTO.setDefaultAcrValues(Set.of(
        clientRegistrationDTO.getDefaultAcrValues().stream().findFirst()
            .orElseThrow(ClientRegistrationServiceException::new)));

    // 9. create and return ClientRegistrationResponseDTO
    return ClientRegistrationResponseDTO.builder()
        .userId(userId)
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
        .clientId(client.getClientId())
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
  public ClientExtended getClientExtendedByClientId(String clientId) {
    ClientExtended clientExtended = clientConnector.getClientExtendedById(clientId)
        .orElseThrow(ClientNotFoundException::new);
    return clientExtended;
  }

  @Override
  public Client getClientByUserId(String userId) {
    return clientConnector.getClientByUserId(userId).orElseThrow(ClientNotFoundException::new);
  }

  @Override
  public void updateClientExtended(ClientRegistrationDTO clientRegistrationDTO,
      ClientExtended clientExtended,
      @Nullable String pdvApiKey,
      @Nullable String planName) {
    Client updatedClient = ClientUtils.convertClientRegistrationDTOToClient(clientRegistrationDTO,
        clientExtended.getUserId());

    String ssmPath = PDV_API_CLIENT_KEY_PREFIX + clientExtended.getClientId();

    // if apiKey not null and pairwise true
    if (updatedClient.isPairwise()) {
      if (!ssmConnectorUtilsImpl.upsertSecureStringIfPresentOnlyIfChanged(ssmPath, pdvApiKey)) {
        Log.errorf("SSM upsert failed %s", ssmPath);
        throw new SSMUpsertPDVException("SSM upsert failed %s: ", ssmPath);
      }
    }
    // delete pdv api key from ssm
    else if (!ssmConnectorUtilsImpl.deleteParameter(ssmPath)) {
      Log.errorf("SSM delete failed %s", ssmPath);
      throw new SSMUpsertPDVException("SSM delete failed %s: ", ssmPath);
    }

    ClientExtended updatedClientExtended = new ClientExtended(updatedClient,
        clientExtended.getSecret(),
        clientExtended.getSalt());
    updatedClientExtended.setClientId(clientExtended.getClientId());
    updatedClientExtended.setUserId(clientExtended.getUserId());
    updatedClientExtended.setAttributeIndex(clientExtended.getAttributeIndex());
    updatedClientExtended.setClientIdIssuedAt(clientExtended.getClientIdIssuedAt());
    clientConnector.updateClientExtended(updatedClientExtended);
  }

  @Override
  public PDVApiKeysDTO getPDVPlanList() {
    String apiKey = ssmConnectorUtilsImpl.getParameter(PDV_API_KEY_PREFIX)
        .orElseThrow(() -> {
          Log.warn("API Key not found: " + PDV_API_KEY_PREFIX);
          return new NoMasterKeyException("API key master not found");
        });

    try {
      return pdvApiClient.getPDVPlans(apiKey);
    } catch (WebApplicationException e) {
      Log.warn("PDV error: " + PDV_API_KEY_PREFIX);
      throw new PDVException("PDV response not ok: ", e);
    }
  }

  @Override
  public PDVValidationResponseDTO validatePDVApiKey(PDVValidateApiKeyDTO validateApiKeyDTO) {
    String apiKey = ssmConnectorUtilsImpl.getParameter(PDV_API_KEY_PREFIX)
        .orElseThrow(() -> {
          Log.warn("API Key not found: " + PDV_API_KEY_PREFIX);
          return new NoMasterKeyException("API key master not found");
        });

    try {
      return pdvApiClient.validatePDVApiKey(validateApiKeyDTO, apiKey);
    } catch (WebApplicationException e) {
      Log.warn("PDV error: " + PDV_API_KEY_PREFIX);
      throw new PDVException("PDV response not ok: ", e);
    }
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
