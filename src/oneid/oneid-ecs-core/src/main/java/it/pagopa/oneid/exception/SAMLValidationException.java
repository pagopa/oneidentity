package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.exception.OneIdentityException;
import it.pagopa.oneid.common.exception.enums.ErrorCode;

public class SAMLValidationException extends OneIdentityException {


  public SAMLValidationException() {
    super(ErrorCode.SAML_VALIDATION_ERROR);
  }

  public SAMLValidationException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public SAMLValidationException(Throwable cause) {
    super(cause);
  }
}
