package it.pagopa.oneid.connector;

import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.GetPublicKeyRequest;
import software.amazon.awssdk.services.kms.model.GetPublicKeyResponse;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

@ApplicationScoped
@CustomLogging
public class KMSConnectorImpl implements KMSConnector {

  @Inject
  KmsClient kmsClient;

  @Override
  public SignResponse sign(String signJWTKeyAlias, byte[] headerBytes, byte[] payloadBytes) {
    // Sign header and payload using KMS Sign operation
    byte[] contentBytes = KMSConnector.concatenateArrays(headerBytes, (byte) '.', payloadBytes);
    SignRequest signRequest = SignRequest.builder()
        .keyId(signJWTKeyAlias)
        .messageType(MessageType.RAW)
        .message(SdkBytes.fromByteArray(contentBytes))
        .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256)
        .build();
    return kmsClient.sign(signRequest);
  }

  @Override
  public GetPublicKeyResponse getPublicKey(String signJWTKeyAlias) {

    GetPublicKeyRequest getPublicKeyRequest = GetPublicKeyRequest.builder()
        .keyId(signJWTKeyAlias)
        .build();
    return kmsClient.getPublicKey(getPublicKeyRequest);
  }
}
