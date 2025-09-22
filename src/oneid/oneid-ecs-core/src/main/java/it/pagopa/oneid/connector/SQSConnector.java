package it.pagopa.oneid.connector;

public interface SQSConnector {

  void sendMessage(String messageBody);
}
