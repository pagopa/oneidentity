package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SAMLValidationException extends RuntimeException {

  private final ErrorCode errorCode;
  
  @Setter
  private String idp;

  @Setter
  private String redirectUri;

  public SAMLValidationException(ErrorCode errorCode) {
    super(errorCode.getErrorMessage());
    this.errorCode = errorCode;
  }

  public SAMLValidationException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }


}
