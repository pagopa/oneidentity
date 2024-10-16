package it.pagopa.oneid.common.model.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class ClientUtilsException extends RuntimeException {

  public ClientUtilsException() {
    super(String.valueOf(ErrorCode.CLIENT_UTILS_ERROR));
  }
}
