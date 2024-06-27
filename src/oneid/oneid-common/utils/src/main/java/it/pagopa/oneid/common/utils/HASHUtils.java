package it.pagopa.oneid.common.utils;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HASHUtils {

  public static String hashSecret(String secret, String salt) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-512");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    md.update(salt.getBytes());

    return new String(md.digest(secret.getBytes(StandardCharsets.UTF_8)));

  }

  public static boolean validateSecret(String secret, String salt, String hashedSecret) {
    return hashSecret(secret, salt).equals(hashedSecret);

  }

}
