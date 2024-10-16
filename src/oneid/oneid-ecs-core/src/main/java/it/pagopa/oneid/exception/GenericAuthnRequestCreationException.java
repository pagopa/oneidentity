package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class GenericAuthnRequestCreationException extends RuntimeException {

  public GenericAuthnRequestCreationException() {
    super(String.valueOf(ErrorCode.GENERIC_AUTHN_REQUEST_CREATION));
  }

  public GenericAuthnRequestCreationException(Throwable cause) {
    super(cause);
  }
}
