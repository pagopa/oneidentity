package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class OIDCAuthorizationException extends RuntimeException {

  public OIDCAuthorizationException() {
    super(String.valueOf(ErrorCode.OIDC_AUTHORIZATION_EXCEPTION));
  }

  public OIDCAuthorizationException(Throwable cause) {
    super(cause);
  }

}
