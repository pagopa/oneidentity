package it.pagopa.oneid.exception;

import lombok.Getter;

@Getter
public class SAMLResponseStatusException extends RuntimeException {

  private final String redirectUri;

  private final String idp;

  private final String clientId;

  public SAMLResponseStatusException(String message, String redirectUri, String clientId,
      String idp) {
    super(message);
    this.redirectUri = redirectUri;
    this.clientId = clientId;
    this.idp = idp;
  }
}
