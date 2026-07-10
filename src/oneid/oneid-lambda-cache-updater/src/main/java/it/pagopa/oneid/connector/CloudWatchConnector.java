package it.pagopa.oneid.connector;

public interface CloudWatchConnector {

  void sendClientCacheUpdateMetricData(String clientID);
}