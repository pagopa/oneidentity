package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import lombok.Getter;

@Getter
public class InvalidClientException extends RuntimeException {

  private final String errorMessage;

  public InvalidClientException(String errorMessage) {
    super(OAuth2Error.INVALID_CLIENT_CODE);
    this.errorMessage = errorMessage;
  }

}
