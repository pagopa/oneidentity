package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.service.utils.ClientConstants.ACS_INDEX_DEFAULT_VALUE;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.exception.ClientUtilsException;
import it.pagopa.oneid.model.dto.ClientMetadataDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ClientUtils {


  public static Client convertClientRegistrationDTOToClient(
      ClientRegistrationRequestDTO clientRegistrationRequestDTO, int maxAttributeIndex) {
    Log.debug("start");

    ClientID clientID = new ClientID(32); //todo length?
    long clientIdIssuedAt = System.currentTimeMillis();

    List<String> requestedParameters = clientRegistrationRequestDTO.getSamlRequestedAttributes()
        .stream()
        .map(Identifier::name)
        .collect(Collectors.toList());

    List<String> callbackUris = clientRegistrationRequestDTO.getRedirectUris()
        .stream()
        .map(URI::toString)
        .toList();

    return Client.builder()
        .clientId(clientID.getValue())
        .friendlyName(clientRegistrationRequestDTO.getClientName())
        .callbackURI(callbackUris)
        .requestedParameters(requestedParameters)
        .authLevel(clientRegistrationRequestDTO.getDefaultAcrValues().getFirst())
        .acsIndex(ACS_INDEX_DEFAULT_VALUE)
        .attributeIndex(maxAttributeIndex + 1)
        .isActive(true)
        .clientIdIssuedAt(clientIdIssuedAt)
        .logoUri(clientRegistrationRequestDTO.getLogoUri())
        .build();
  }

  public static String generateClientSecret() throws NoSuchAlgorithmException {
    Log.debug("start");

    KeyGenerator keyGen = KeyGenerator.getInstance(ClientConstants.ENCRYPTION_ALGORITHM);
    keyGen.init(ClientConstants.CLIENT_SECRET_BIT_LENGTH); // Use 256-bit key
    SecretKey secretKey = keyGen.generateKey();
    byte[] rawData = secretKey.getEncoded();
    return Base64.getEncoder().encodeToString(rawData);
  }

  public static String encryptSalt(String salt, String encodedSecret)
      throws ClientUtilsException {
    Log.debug("start");

    // Rebuild the key using SecretKeySpec
    Log.debug("rebuild the key using SecretKeySpec");

    byte[] decodedSecret = Base64.getDecoder()
        .decode(encodedSecret.getBytes(StandardCharsets.UTF_8));
    SecretKey secretKey = new SecretKeySpec(decodedSecret, 0, decodedSecret.length,
        ClientConstants.ENCRYPTION_ALGORITHM);

    // Initialize the cipher
    Log.debug("initialize the cipher");
    Cipher cipher;
    try {
      cipher = Cipher.getInstance(ClientConstants.ENCRYPTION_ALGORITHM);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new ClientUtilsException();
    }
    try {
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    } catch (InvalidKeyException e) {
      throw new ClientUtilsException();
    }

    // Encrypt the data
    Log.debug("encrypt the data");
    byte[] encryptedBytes;
    try {
      encryptedBytes = cipher.doFinal(salt.getBytes(StandardCharsets.UTF_8));
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new ClientUtilsException();
    }

    // Encode the result as Base64
    Log.debug("end");
    return Base64.getEncoder().encodeToString(encryptedBytes);
  }


  public static ClientMetadataDTO convertClientToClientMetadataDTO(Client client) {
    Log.debug("start");
    List<Identifier> samlRequestedAttributes = client
        .getRequestedParameters()
        .stream()
        .map(Identifier::valueOf)
        .collect(Collectors.toList());

    List<URI> callbackUris = client
        .getCallbackURI()
        .stream()
        .map(clientUri -> {
          try {
            return new URI(clientUri);
          } catch (URISyntaxException e) {
            throw new ClientUtilsException();
          }
        })
        .toList();

    return ClientMetadataDTO.builder()
        .redirectUris(callbackUris)
        .clientName(client.getFriendlyName())
        .logoUri(client.getLogoUri())
        .defaultAcrValues(List.of(client.getAuthLevel()))
        .samlRequestedAttributes(samlRequestedAttributes)
        .build();

  }

}
