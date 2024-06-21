package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class SessionException extends OneIdentityException {

  public SessionException() {
    super(ErrorCode.SESSION_ERROR);
  }

  public SessionException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public SessionException(Throwable cause) {
    super(cause);
  }
}
