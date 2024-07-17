package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class UnsupportedResponseTypeException extends AuthorizationErrorException {

  public UnsupportedResponseTypeException(String callbackUri, String state) {
    super(String.valueOf(ErrorCode.UNSUPPORTED_RESPONSE_TYPE_ERROR), callbackUri, state);
  }
}