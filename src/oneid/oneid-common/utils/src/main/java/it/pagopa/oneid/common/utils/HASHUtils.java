package it.pagopa.oneid.common.utils;


import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.validation.constraints.NotBlank;
import java.security.SecureRandom;
import java.util.Base64;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

@CustomLogging
public class HASHUtils {

  public static final int ARGON_2_ITERATIONS_NUMBER = 2;
  public static final int ARGON_2_MEM_LIMIT = 66536;
  public static final int ARGON_2_HASH_LENGTH = 32;
  public static final int ARGON_2_PARALLELISM = 4;

  public static final Base64.Encoder b64encoder = Base64.getEncoder().withoutPadding();

  public static final Base64.Decoder b64decoder = Base64.getDecoder();

  public static boolean validateSecret(String salt, String secret, String hashedSecret) {
    return generateArgon2(b64decoder.decode(salt), b64decoder.decode(secret)).equals(hashedSecret);
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


}
