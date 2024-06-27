package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class OIDCSignJWTException extends OneIdentityException {

  public OIDCSignJWTException() {
    super(ErrorCode.SIGN_JWT_ERROR);
  }

  public OIDCSignJWTException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public OIDCSignJWTException(Throwable cause) {
    super(cause);
  }

}
