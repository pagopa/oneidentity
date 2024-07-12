package it.pagopa.oneid.common.model.exception.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

  MARSHALLER_ERROR("MARSHALLER_ERROR", "Empty marshaller"),
  IDP_SSO_ENDPOINT_NOT_FOUND("IDP_SSO_ENDPOINT_NOT_FOUND", "Unable to find the idp-sso endpoint"),
  GENERIC_AUTHN_REQUEST_CREATION("GENERIC_AUTHN_REQUEST_CREATION",
      "Generic AuthnRequest Exception"),
  SAML_UTILS_ERROR("SAML_UTILS_ERROR", "Generic exception inside SAMLUtils"),
  CALLBACK_URI_NOT_FOUND("CALLBACK_URI_NOT_FOUND",
      "Callback URI not found from in Client metadata"),
  CLIENT_NOT_FOUND("CLIENT_NOT_FOUND", "Client not registered"),
  ASSERTION_NOT_FOUND("ASSERTION_NOT_FOUND", "Assertion not found"),
  IDP_NOT_FOUND("IDP_NOT_FOUND", ""),
  SESSION_ERROR("SESSION_ERROR", ""),
  SAML_VALIDATION_ERROR("SAML_VALIDATION_ERROR", ""),
  SIGN_JWT_ERROR("SIGN_JWT_ERROR", ""),
  OIDC_AUTHORIZATION_EXCEPTION("OIDC_AUTHORIZATION_EXCEPTION", ""),
  SAML_RESPONSE_STATUS_ERROR("SAML_RESPONSE_STATUS_ERROR", ""),
  GENERIC_HTML_ERROR("GENERIC_HTML_ERROR", ""),
  ERRORCODE_NR19("19", ""),
  ERRORCODE_NR20("20", ""),
  ERRORCODE_NR21("21", ""),
  ERRORCODE_NR22("22", ""),
  ERRORCODE_NR23("23", ""),
  ERRORCODE_NR25("25", "");


  private final String errorCode;
  private final String errorMessage;

  ErrorCode(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }
}
