package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.token.BearerTokenError;

public class InvalidAccessTokenException extends RuntimeException {

  public InvalidAccessTokenException() {
    super(BearerTokenError.INVALID_TOKEN.getCode());
  }
}
