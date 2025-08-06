package it.pagopa.oneid.exception;

import it.pagopa.oneid.model.enums.ClientRegistrationErrorCode;
import lombok.Getter;

@Getter
public class InvalidUriException extends RuntimeException {

  private final ClientRegistrationErrorCode clientRegistrationErrorCode;
  private final String message;


  public InvalidUriException(
      String message) {
    super();
    this.clientRegistrationErrorCode = ClientRegistrationErrorCode.INVALID_CLIENT_REGISTRATION;
    this.message = message;
  }

  public InvalidUriException(ClientRegistrationErrorCode clientRegistrationErrorCode) {
    super();
    this.clientRegistrationErrorCode = clientRegistrationErrorCode;
    this.message = clientRegistrationErrorCode.getErrorMessage();
  }
}
