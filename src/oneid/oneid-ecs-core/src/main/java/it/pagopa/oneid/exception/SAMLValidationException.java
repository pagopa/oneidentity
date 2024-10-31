package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class SAMLValidationException extends RuntimeException {

  private ErrorCode errorCode;

  public SAMLValidationException() {
    super(String.valueOf(ErrorCode.SAML_VALIDATION_ERROR));
  }

  public SAMLValidationException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public SAMLValidationException(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public SAMLValidationException(Throwable cause) {
    super(cause);
  }
}
