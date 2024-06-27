package it.pagopa.oneid.connector;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

@ApplicationScoped
public class KMSConnectorImpl implements KMSConnector {

  @ConfigProperty(name = "kms_key_id")
  private static String KMS_KEY_ID;
  
  @Inject
  KmsClient kmsClient;

  @Inject
  KMSConnectorImpl(@ConfigProperty(name = "kms_key_id") String kmsKeyID) {
    KMS_KEY_ID = kmsKeyID;
  }

  @Override
  public SignResponse sign(byte[] headerBytes, byte[] payloadBytes) {
    // Sign header and payload using KMS Sign operation
    byte[] contentBytes = KMSConnector.concatenateArrays(headerBytes, (byte) '.', payloadBytes);
    SignRequest signRequest = SignRequest.builder()
        .keyId(KMS_KEY_ID)
        .messageType(MessageType.RAW)
        .message(SdkBytes.fromByteArray(contentBytes))
        .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256)
        .build();
    return kmsClient.sign(signRequest);
  }
}
