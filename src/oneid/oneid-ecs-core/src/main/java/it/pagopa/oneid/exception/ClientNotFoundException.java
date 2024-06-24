package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class ClientNotFoundException extends OneIdentityException {

  public ClientNotFoundException() {
    super(ErrorCode.CLIENT_NOT_FOUND);
  }

  public ClientNotFoundException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public ClientNotFoundException(Throwable cause) {
    super(cause);
  }
}
