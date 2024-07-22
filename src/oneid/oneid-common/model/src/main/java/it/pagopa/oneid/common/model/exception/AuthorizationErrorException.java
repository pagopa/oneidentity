package it.pagopa.oneid.common.model.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class AuthorizationErrorException extends RuntimeException {

  private String callbackUri;
  private String state;

  public AuthorizationErrorException(String callbackUri, String state) {
    super(String.valueOf(ErrorCode.AUTHORIZATION_ERROR));
    this.callbackUri = callbackUri;
    this.state = state;
  }

  public AuthorizationErrorException(String errorCode, String callbackUri, String state) {
    super(errorCode);
    this.callbackUri = callbackUri;
    this.state = state;
  }

  public AuthorizationErrorException() {
  }
}
