package it.pagopa.oneid.connector;

import software.amazon.awssdk.services.kms.model.GetPublicKeyResponse;
import software.amazon.awssdk.services.kms.model.SignResponse;

public interface KMSConnector {

  static byte[] concatenateArrays(byte[] arr1, byte delimiter, byte[] arr2) {
    byte[] result = new byte[arr1.length + 1 + arr2.length];
    System.arraycopy(arr1, 0, result, 0, arr1.length);
    result[arr1.length] = delimiter;
    System.arraycopy(arr2, 0, result, arr1.length + 1, arr2.length);
    return result;
  }

  SignResponse sign(byte[] headerBytes, byte[] payloadBytes);

  GetPublicKeyResponse getPublicKey();
}
