package it.pagopa.oneid.exception;

import it.pagopa.oneid.model.enums.ClientRegistrationErrorCode;
import lombok.Getter;

@Getter
public class InvalidLogoURIException extends RuntimeException {

  private final ClientRegistrationErrorCode clientRegistrationErrorCode;

  public InvalidLogoURIException() {
    super();
    this.clientRegistrationErrorCode = ClientRegistrationErrorCode.INVALID_CLIENT_METADATA;
  }
}
