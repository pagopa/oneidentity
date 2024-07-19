package it.pagopa.oneid.model.enums;

import lombok.Getter;

@Getter
public enum ClientRegistrationErrorCode {

  INVALID_REDIRECT_URI("invalid_redirect_uri", "One or more redirect_uri values are invalid"),
  REDIRECT_URI_NULL("invalid_redirect_uri", "One or more redirect_uri values are empty"),
  INVALID_CLIENT_METADATA("invalid_client_metadata", "Invalid client Metadata");

  private final String errorCode;
  private final String errorMessage;

  ClientRegistrationErrorCode(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

}
