package it.pagopa.oneid.connector;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public interface CloudWatchConnector {

  void sendIDPErrorMetricData(String IDP, ErrorCode errorCode);

  void sendIDPSuccessMetricData(String IDP);

  void sendClientErrorMetricData(String clientID, ErrorCode errorCode);

  void sendClientSuccessMetricData(String ClientID);

}