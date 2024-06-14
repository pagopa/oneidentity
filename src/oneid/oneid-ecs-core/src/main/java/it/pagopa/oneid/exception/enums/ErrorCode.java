package it.pagopa.oneid.exception.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    MARSHALLER_ERROR("MARSHALLER_ERROR", "Empty marshaller"),
    IDPSSOENDPOINT_NOT_FOUND("IDPSSOENDPOINT_NOT_FOUND", "Unable to find the idp-sso endpoint"),
    GENERIC_AUTHN_REQUEST_CREATION_EXCEPTION("GENERIC_AUTHN_REQUEST_CREATION_EXCEPTION", "Generic AuthnRequest Exception"),
    SAMLUTILS_EXCEPTION("SAMLUTILS_EXCEPTION", "Generic exception inside SAMLUtils");


    private final String errorCode;
    private final String errorMessage;

    ErrorCode(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
