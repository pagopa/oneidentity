package it.pagopa.oneid.connector;

public interface PublicIdpsBucketConnector {

  void uploadIdpsJson(String objectKey, String content);

}
