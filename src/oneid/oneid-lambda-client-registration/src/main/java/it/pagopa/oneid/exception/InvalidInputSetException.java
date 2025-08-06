package it.pagopa.oneid.exception;

import lombok.Getter;

@Getter
public class InvalidInputSetException extends RuntimeException {

  public InvalidInputSetException(String message) {
    super(message);
  }
}
