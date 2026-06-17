package it.pagopa.oneid.exception;

public class InvalidClientInput extends RuntimeException {

  public InvalidClientInput(String customErrorMessage) {
    super(customErrorMessage);
  }
}
