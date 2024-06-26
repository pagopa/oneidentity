package it.pagopa.oneid.connector;

public interface KMSConnector {

  static byte[] concatenateArrays(byte[] arr1, byte delimiter, byte[] arr2) {
    byte[] result = new byte[arr1.length + 1 + arr2.length];
    System.arraycopy(arr1, 0, result, 0, arr1.length);
    result[arr1.length] = delimiter;
    System.arraycopy(arr2, 0, result, arr1.length + 1, arr2.length);
    return result;
  }

  String sign(byte[] headerBytes, byte[] payloadBytes);
}
