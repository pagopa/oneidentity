package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class GenericHTMLException extends RuntimeException {

  private final String redirectUri;
  private final String state;
  private final String clientId;

  public GenericHTMLException(ErrorCode errorCode) {
    this(errorCode, null, null, null);
  }

  public GenericHTMLException(ErrorCode errorCode, String redirectUri, String state,
      String clientId) {
    super(String.valueOf(errorCode));
    this.redirectUri = redirectUri;
    this.state = state;
    this.clientId = clientId;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public String getState() {
    return state;
  }

  public String getClientId() {
    return clientId;
  }
}
