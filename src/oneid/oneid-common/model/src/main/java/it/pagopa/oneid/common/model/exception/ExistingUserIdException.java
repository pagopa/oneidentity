package it.pagopa.oneid.common.model.exception;

public class ExistingUserIdException extends RuntimeException {

  public ExistingUserIdException(String message) {
    super(message);
  }
}
