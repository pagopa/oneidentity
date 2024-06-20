package it.pagopa.oneid.exception;

import it.pagopa.oneid.exception.enums.ErrorCode;

public class SAMLResponseStatusException extends OneIdentityException {

  public SAMLResponseStatusException(ErrorCode e) {
    super(e);
  }

  public SAMLResponseStatusException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public SAMLResponseStatusException(Throwable cause) {
    super(cause);
  }
}
