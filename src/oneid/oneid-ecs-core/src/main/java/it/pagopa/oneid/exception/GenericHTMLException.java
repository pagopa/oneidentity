package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class GenericHTMLException extends RuntimeException {
  
  public GenericHTMLException(ErrorCode errorCode) {
    super(String.valueOf(errorCode));
  }
}
