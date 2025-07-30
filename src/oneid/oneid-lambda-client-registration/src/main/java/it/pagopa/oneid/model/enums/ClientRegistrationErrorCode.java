package it.pagopa.oneid.model.enums;

import lombok.Getter;

@Getter
public enum ClientRegistrationErrorCode {

  INVALID_URI("invalid_uri", "Invalid URI provided"),
  EMPTY_URI("invalid_uri", "Empty or null URI provided"),
  INVALID_CLIENT_REGISTRATION("invalid_client_registration", "Invalid ClientRegistration");

  private final String errorCode;
  private final String errorMessage;

  ClientRegistrationErrorCode(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

}
