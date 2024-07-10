package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class GenericHTMLException extends OneIdentityException {

  public GenericHTMLException() {
    super(ErrorCode.GENERIC_HTML_ERROR);
  }

  public GenericHTMLException(String customErrorMessage) {
    super(customErrorMessage);
  }

  public GenericHTMLException(Throwable cause) {
    super(cause);
  }
}
