package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class AssertionNotFoundException extends RuntimeException {

  public AssertionNotFoundException() {
    super(String.valueOf(ErrorCode.ASSERTION_NOT_FOUND));
  }

  public AssertionNotFoundException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public AssertionNotFoundException(Throwable cause) {
    super(cause);
  }

}
