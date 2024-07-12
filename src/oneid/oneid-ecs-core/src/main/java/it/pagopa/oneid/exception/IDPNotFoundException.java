package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class IDPNotFoundException extends RuntimeException {

  public IDPNotFoundException() {
    super(String.valueOf(ErrorCode.IDP_NOT_FOUND));
  }
}
