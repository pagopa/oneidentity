package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.exception.OneIdentityException;
import it.pagopa.oneid.common.exception.enums.ErrorCode;

public class IDPNotFoundException extends OneIdentityException {

  public IDPNotFoundException() {
    super(ErrorCode.IDPNOT_FOUND);
  }

  public IDPNotFoundException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public IDPNotFoundException(Throwable cause) {
    super(cause);
  }
}
