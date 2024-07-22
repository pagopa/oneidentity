package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;

public class UnsupportedGrantTypeException extends RuntimeException {

  public UnsupportedGrantTypeException() {
    super(OAuth2Error.UNSUPPORTED_GRANT_TYPE_CODE);
  }

}
