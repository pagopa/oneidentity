package it.pagopa.oneid.exception;

public class RefreshSecretException extends RuntimeException {
  
  public RefreshSecretException(String customErrorMessage) {
    super(customErrorMessage);
  }
}
