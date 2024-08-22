package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.CreateKeyRequest;
import software.amazon.awssdk.services.kms.model.CreateKeyResponse;
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

    assertFalse(signResponse.toString().isEmpty());
  }
}
