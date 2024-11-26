package it.pagopa.oneid.connector;

public interface CloudWatchConnector {

  void sendIDPErrorMetricData(String IDP, String errorMessage);

  void sendIDPSuccessMetricData(String IDP);

  void sendClientErrorMetricData(String clientID, String errorMessage);

  void sendClientSuccessMetricData(String ClientID);

}
