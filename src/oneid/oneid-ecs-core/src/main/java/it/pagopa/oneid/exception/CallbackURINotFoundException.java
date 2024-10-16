package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;


public class CallbackURINotFoundException extends RuntimeException {

  public CallbackURINotFoundException() {
    super(String.valueOf(ErrorCode.CALLBACK_URI_NOT_FOUND));
  }

}
