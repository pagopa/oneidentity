package it.pagopa.oneid.exception;

import lombok.Getter;

@Getter
public class SAMLResponseStatusException extends RuntimeException {

  private final String redirectUri;

  public SAMLResponseStatusException(String message, String redirectUri) {
    super(message);
    this.redirectUri = redirectUri;
  }
}
