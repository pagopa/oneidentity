package it.pagopa.oneid.exception;

public class SSMUpsertPDVException extends RuntimeException {

  public SSMUpsertPDVException(String message, String ssmPath) {
    super(message + " [path=" + ssmPath + "]");
  }
}