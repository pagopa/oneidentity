package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class SAMLValidationException extends RuntimeException {

  private final ErrorCode errorCode;

  public SAMLValidationException(ErrorCode errorCode) {
    super(errorCode.getErrorMessage());
    this.errorCode = errorCode;
  }

  public SAMLValidationException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

}
