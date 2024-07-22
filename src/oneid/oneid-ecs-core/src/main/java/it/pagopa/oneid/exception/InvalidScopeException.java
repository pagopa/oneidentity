package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class InvalidScopeException extends AuthorizationErrorException {

  public InvalidScopeException(String callbackUri, String state) {
    super(String.valueOf(ErrorCode.INVALID_SCOPE_ERROR), callbackUri, state);
  }
}
