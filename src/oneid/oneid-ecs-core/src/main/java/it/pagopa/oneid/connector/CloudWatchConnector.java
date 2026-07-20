package it.pagopa.oneid.connector;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public interface CloudWatchConnector {

  void sendSAMLStatusRelatedErrorMetricData(String IDP, String client, String errorCode);

  void sendIDPErrorMetricData(String IDP, ErrorCode errorCode);

  void sendXSWAssertionErrorMetricData();

  void sendIDPSuccessMetricData(String IDP);

  void sendClientErrorMetricData(String clientID, ErrorCode errorCode);

  void sendClientCacheMissMetricData(String clientID);

  void sendClientCacheBackfillSuccessMetricData(String clientID);

  void sendClientCacheBackfillFailureMetricData(String clientID);

  void sendOIDynamoDBErrorMetricData(int numAttempts);

  void sendOISQSErrorMetricData();

  void sendPDVErrorMetricData(int statusCode);

  void sendClientSuccessMetricData(String ClientID);

  void sendUserInfoSuccessMetricData(String clientID);

  void sendUserInfoSuccessWithoutPairwiseMetricData(String clientID);

  void sendUserInfoErrorMetricData(String clientID);

}
