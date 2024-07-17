package it.pagopa.oneid.common.utils;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class HASHUtils {

  public static String hashSecret(String secret, String salt) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-512");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    md.update(salt.getBytes(StandardCharsets.UTF_8));
    byte[] bytes = md.digest(secret.getBytes(StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    for (byte aByte : bytes) {
      sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
    }

    return sb.toString();

  }

  public static boolean validateSecret(String secret, String salt, String hashedSecret) {
    return hashSecret(secret, salt).equals(hashedSecret);

  }

  public static String generateRandomString(int lengthInBytes) {
    SecureRandom random = new SecureRandom();
    byte[] saltBytes = new byte[lengthInBytes];
    random.nextBytes(saltBytes);
    return Base64.getEncoder().encodeToString(saltBytes);
  }

}
