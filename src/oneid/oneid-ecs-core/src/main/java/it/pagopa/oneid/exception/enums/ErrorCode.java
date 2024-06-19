package it.pagopa.oneid.exception.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

  MARSHALLER_ERROR("MARSHALLER_ERROR", "Empty marshaller"),
  IDPSSOENDPOINT_NOT_FOUND("IDPSSOENDPOINT_NOT_FOUND", "Unable to find the idp-sso endpoint"),
  GENERIC_AUTHN_REQUEST_CREATION("GENERIC_AUTHN_REQUEST_CREATION",
      "Generic AuthnRequest Exception"),
  SAMLUTILS_ERROR("SAMLUTILS_ERROR", "Generic exception inside SAMLUtils"),
  CALLBACK_URINOT_FOUND("CALLBACK_URINOT_FOUND", ""),
  CLIENT_NOT_FOUND("CLIENT_NOT_FOUND", ""),
  IDPNOT_FOUND("IDPNOT_FOUND", ""),
  SESSION_ERROR("SESSION_ERROR", ""),
  SAML_VALIDATION_ERROR("SAML_VALIDATION_ERROR", ""),
  SAML_RESPONSE_STATUS_ERROR("SAML_RESPONSE_STATUS_ERROR", "");


  private final String errorCode;
  private final String errorMessage;

  ErrorCode(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }
}
