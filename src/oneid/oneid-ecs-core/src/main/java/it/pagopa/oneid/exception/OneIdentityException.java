package it.pagopa.oneid.exception;

import it.pagopa.oneid.exception.enums.ErrorCode;


public class OneIdentityException extends Exception {

  public OneIdentityException(ErrorCode e) {
    super(e.getErrorMessage());
  }

  public OneIdentityException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public OneIdentityException(Throwable cause) {
    super(cause);
  }


}
