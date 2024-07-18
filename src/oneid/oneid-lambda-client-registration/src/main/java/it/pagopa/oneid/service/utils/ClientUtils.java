package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.service.utils.ClientConstants.ACS_INDEX_DEFAULT_VALUE;
import com.nimbusds.oauth2.sdk.id.ClientID;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.exception.ClientUtilsException;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    ClientID clientID = new ClientID(32); //todo length?
    long clientIdIssuedAt = System.currentTimeMillis();

    List<String> requestedParameters = clientRegistrationRequestDTO.getSamlRequestedAttributes()
        .stream()
        .map(Identifier::name)
        .collect(Collectors.toList());

    return Client.builder()
        .clientId(clientID.getValue())
        .friendlyName(clientRegistrationRequestDTO.getClientName())
        .callbackURI(clientRegistrationRequestDTO.getRedirectUris())
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

    KeyGenerator keyGen = KeyGenerator.getInstance(ClientConstants.ENCRYPTION_ALGORITHM);
    keyGen.init(ClientConstants.CLIENT_SECRET_BIT_LENGTH); // Use 256-bit key
    SecretKey secretKey = keyGen.generateKey();
    return new String(secretKey.getEncoded());
  }

  public static String encryptSalt(String salt, String secret)
      throws ClientUtilsException {

    // Rebuild the key using SecretKeySpec
    SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), 0,
        secret.length(),
        ClientConstants.ENCRYPTION_ALGORITHM);

    // Initialize the cipher
    Cipher cipher;
    try {
      cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new ClientUtilsException();
    }
    try {
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    } catch (InvalidKeyException e) {
      throw new ClientUtilsException();
    }

    // Your plaintext data

    // Encrypt the data
    byte[] encryptedBytes;
    try {
      encryptedBytes = cipher.doFinal(salt.getBytes(StandardCharsets.UTF_8));
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new ClientUtilsException();
    }

    // Encode the result as Base64
    return new String(encryptedBytes);
  }
  
}
