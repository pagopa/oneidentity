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
  IDP_NOT_FOUND("IDP_NOT_FOUND", "IDP not found"),
  SESSION_ERROR("SESSION_ERROR", "Error during session handling"),
  SAML_VALIDATION_ERROR("SAML_VALIDATION_ERROR", "Error during SAML Response validation"),
  SIGN_JWT_ERROR("SIGN_JWT_ERROR", "Error signing JWT"),
  OIDC_AUTHORIZATION_EXCEPTION("OIDC_AUTHORIZATION_EXCEPTION", "Error during authorization"),
  SAML_RESPONSE_STATUS_ERROR("SAML_RESPONSE_STATUS_ERROR", "Error found in SAML Response status"),
  GENERIC_HTML_ERROR("GENERIC_HTML_ERROR", "Error during execution"),
  AUTHORIZATION_ERROR("AUTHORIZATION_ERROR", "Authorization error"),
  AUTHORIZATION_ERROR_IDP("AUTHORIZATION_ERROR_IDP", "IDP not selected"),
  AUTHORIZATION_ERROR_RESPONSE_TYPE("AUTHORIZATION_ERROR_RESPONSE_TYPE",
      "Response type not selected"),
  INVALID_SCOPE_ERROR("INVALID_SCOPE_ERROR", "Scope not supported"),
  INVALID_RESPONSE_TYPE_ERROR("INVALID_RESPONSE_TYPE", "Response type not valid"),
  UNSUPPORTED_RESPONSE_TYPE_ERROR("UNSUPPORTED_RESPONSE_TYPE_ERROR", "Response type not supported"),
  ERRORCODE_NR19("19", ""),
  ERRORCODE_NR20("20", ""),
  ERRORCODE_NR21("21", ""),
  ERRORCODE_NR22("22", ""),
  ERRORCODE_NR23("23", ""),
  ERRORCODE_NR25("25", ""),
  CLIENT_UTILS_ERROR("CLIENT_UTILS_ERROR", "Error during Client credentials initialization"),
  CLIENT_REGISTRATION_SERVICE_ERROR("CLIENT_REGISTRATION_SERVICE_ERROR",
      "Error during ClientRegistrationService method");


  private final String errorCode;
  private final String errorMessage;

  ErrorCode(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }
}
