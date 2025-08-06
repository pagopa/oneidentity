package it.pagopa.oneid.exception;

import lombok.Getter;

@Getter
public class InvalidLocalizedContentMapException extends RuntimeException {

  public InvalidLocalizedContentMapException(String message) {
    super(message);
  }
}
