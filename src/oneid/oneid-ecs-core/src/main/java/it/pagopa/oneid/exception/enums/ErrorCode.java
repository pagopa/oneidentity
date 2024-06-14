package it.pagopa.oneid.exception.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    MARSHALLER_ERROR("MARSHALLER_ERROR", "Empty marshaller"),
    IDPSSOENDPOINT_NOT_FOUND("IDPSSOENDPOINT_NOT_FOUND", "message custom"), //FIXME CHANGE THIS
    GENERIC_AUTHN_REQUEST_CREATION_EXCEPTION("GENERIC_AUTHN_REQUEST_CREATION_EXCEPTION", "message custom"); //FIXME CHANGE THIS


    private final String errorCode;
    private final String errorMessage;

    ErrorCode(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
