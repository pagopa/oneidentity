package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;

public class InvalidGrantException extends RuntimeException {

  public InvalidGrantException() {
    super(OAuth2Error.INVALID_GRANT_CODE);
  }

}
