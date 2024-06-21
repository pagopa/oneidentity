package it.pagopa.oneid.common.exception;


import it.pagopa.oneid.common.exception.enums.ErrorCode;

public class SAMLUtilsException extends OneIdentityException {

  public SAMLUtilsException() {
    super(ErrorCode.SAMLUTILS_ERROR);
  }

  public SAMLUtilsException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public SAMLUtilsException(Throwable cause) {
    super(cause);
  }
}
