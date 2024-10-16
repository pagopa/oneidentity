package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class SAMLValidationException extends RuntimeException {


  public SAMLValidationException() {
    super(String.valueOf(ErrorCode.SAML_VALIDATION_ERROR));
  }

  public SAMLValidationException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public SAMLValidationException(Throwable cause) {
    super(cause);
  }
}
