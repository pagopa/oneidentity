package it.pagopa.oneid.common.utils;


import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.ClientUtilsException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {


  public static String hashSalt(String salt) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance(ClientConstants.HASHING_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    byte[] bytes = md.digest(salt.getBytes(StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    for (byte aByte : bytes) {
      sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
    }

    return sb.toString();
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

  public static boolean validateSecret(String salt, String encodedSecret, String encryptedSalt) {
    return encryptSalt(salt, encodedSecret).equals(encryptedSalt);
  }

  public static String generateRandomString(int lengthInBytes) {
    SecureRandom random = new SecureRandom();
    byte[] saltBytes = new byte[lengthInBytes];
    random.nextBytes(saltBytes);
    return Base64.getEncoder().encodeToString(saltBytes);
  }


}
