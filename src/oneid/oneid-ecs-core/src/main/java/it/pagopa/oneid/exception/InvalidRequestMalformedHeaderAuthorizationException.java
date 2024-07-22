package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;

public class InvalidRequestMalformedHeaderAuthorizationException extends RuntimeException {

  public InvalidRequestMalformedHeaderAuthorizationException() {
    super(OAuth2Error.INVALID_REQUEST_CODE);
  }
}
