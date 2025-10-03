package it.pagopa.oneid.exception;

import lombok.Getter;

@Getter
public class InvalidBearerTokenException extends RuntimeException {

  public InvalidBearerTokenException(String customErrorMessage) {
    super(customErrorMessage);
  }
}
