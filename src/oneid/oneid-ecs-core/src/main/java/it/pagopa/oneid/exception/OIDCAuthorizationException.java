package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class OIDCAuthorizationException extends OneIdentityException {

  public OIDCAuthorizationException() {
    super(ErrorCode.OIDCAUTHORIZATION_EXCEPTION);
  }

  public OIDCAuthorizationException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public OIDCAuthorizationException(Throwable cause) {
    super(cause);
  }

}
