package it.pagopa.oneid.exception;

import it.pagopa.oneid.model.enums.ClientRegistrationErrorCode;
import lombok.Getter;

@Getter
public class InvalidRedirectURIException extends RuntimeException {

  private final ClientRegistrationErrorCode clientRegistrationErrorCode;

  public InvalidRedirectURIException(ClientRegistrationErrorCode clientRegistrationErrorCode) {
    super();
    this.clientRegistrationErrorCode = clientRegistrationErrorCode;
  }
}
