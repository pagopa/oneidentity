package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.CreateKeyRequest;
import software.amazon.awssdk.services.kms.model.CreateKeyResponse;
import software.amazon.awssdk.services.kms.model.GetPublicKeyResponse;
import software.amazon.awssdk.services.kms.model.KeySpec;
import software.amazon.awssdk.services.kms.model.KeyUsageType;
import software.amazon.awssdk.services.kms.model.SignResponse;

@QuarkusTest
public class KMSConnectorImplTest {

  private static String kmsKeyID;
  @Inject
  KMSConnectorImpl kmsConnectorImpl;
  @Inject
  KmsClient kmsClient;

  @BeforeEach
  void beforeEach() {
    CreateKeyResponse result = kmsClient.createKey(CreateKeyRequest.
        builder()
        .keySpec(KeySpec.RSA_2048)
        .keyUsage(KeyUsageType.SIGN_VERIFY)
        .build());
    kmsKeyID = result.keyMetadata().keyId();
  }

  @Test
  void sign() {
    // given
    String header = "testHeader";
    String payload = "testPayload";

    SignResponse signResponse = kmsConnectorImpl.sign(kmsKeyID,
        header.getBytes(StandardCharsets.UTF_8),
        payload.getBytes(StandardCharsets.UTF_8));

    // then
    assertFalse(signResponse.toString().isEmpty());
  }

  @Test
  void signWithNotExistingKeyID() {
    // given
    String header = "testHeader";
    String payload = "testPayload";
    String dummyKeyID = "dummy";

    Executable executable = () -> kmsConnectorImpl.sign(dummyKeyID,
        header.getBytes(StandardCharsets.UTF_8),
        payload.getBytes(StandardCharsets.UTF_8));

    // then
    assertThrows(Exception.class, executable);
  }

  @Test
  void getPublicKey() {
    // given
    GetPublicKeyResponse publicKeyResponse = kmsConnectorImpl.getPublicKey(kmsKeyID);

    // then
    assertFalse(publicKeyResponse.publicKey().toString().isEmpty());
  }

  @Test
  void getNotExistingPublicKey() {
    // given
    String dummyKeyID = "dummy";
    Executable executable = () -> kmsConnectorImpl.getPublicKey(dummyKeyID);

    // then
    assertThrows(Exception.class, executable);
  }
}
