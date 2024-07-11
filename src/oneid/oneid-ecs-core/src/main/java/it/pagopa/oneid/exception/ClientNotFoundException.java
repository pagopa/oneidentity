package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class ClientNotFoundException extends RuntimeException {

  public ClientNotFoundException() {
    super(String.valueOf(ErrorCode.CLIENT_NOT_FOUND));
  }
  
}
