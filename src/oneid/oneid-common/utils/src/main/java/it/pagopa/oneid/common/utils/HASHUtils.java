package it.pagopa.oneid.common.utils;


import io.quarkus.logging.Log;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.encoders.Hex;

@CustomLogging
public class HASHUtils {

  public static final int ARGON_2_ITERATIONS_NUMBER = 2;
  public static final int ARGON_2_MEM_LIMIT = 19000;
  public static final int ARGON_2_HASH_LENGTH = 32;
  public static final int ARGON_2_PARALLELISM = 4;

  public static final Base64.Encoder b64encoder = Base64.getEncoder().withoutPadding();

  public static final Base64.Decoder b64decoder = Base64.getDecoder();

  public static boolean validateSecret(String salt, String secret, String hashedSecret) {
    byte[] saltBytes;

    try {
      saltBytes = b64decoder.decode(salt);
    } catch (IllegalArgumentException e) {
      Log.error("Invalid salt value: " + salt);
      throw new RuntimeException(e);
    }

    byte[] secretBytes;

    try {
      secretBytes = b64decoder.decode(secret);
    } catch (IllegalArgumentException e) {
      Log.error("Invalid secret value");
      return false;
    }

    return generateArgon2(saltBytes, secretBytes).equals(hashedSecret);
  }

  public static byte[] generateSecureRandom(int bytesLength) {
    SecureRandom secureRandom = new SecureRandom();
    byte[] result = new byte[bytesLength];
    secureRandom.nextBytes(result);
    return result;
  }

  public static String generateArgon2(@NotBlank byte[] salt, @NotBlank byte[] clientSecret) {

    Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
        .withVersion(Argon2Parameters.ARGON2_VERSION_13)
        .withIterations(ARGON_2_ITERATIONS_NUMBER)
        .withMemoryAsKB(ARGON_2_MEM_LIMIT)
        .withParallelism(ARGON_2_PARALLELISM)
        .withSalt(salt);

    Argon2BytesGenerator generate = new Argon2BytesGenerator();
    generate.init(builder.build());
    byte[] result = new byte[ARGON_2_HASH_LENGTH];
    generate.generateBytes(clientSecret, result, 0, result.length);
    return b64encoder.encodeToString(result);

  }

  public static String generateIDHash(String id) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-512");
    } catch (NoSuchAlgorithmException e) {
      Log.error("Error during hashId creation: " + e.getMessage());
      throw new RuntimeException(e);
    }
    byte[] hash = digest.digest(
        id.getBytes(StandardCharsets.UTF_8));
    return new String(Hex.encode(hash));
  }

}
