package it.pagopa.oneid.exception;


import it.pagopa.oneid.exception.enums.ErrorCode;

public class SAMLUtilsException extends OneIdentityException {

    public SAMLUtilsException() {
        super(ErrorCode.SAMLUTILS_EXCEPTION);
    }

    public SAMLUtilsException(String customErrorMessage) {
        super(customErrorMessage);
    }

    public SAMLUtilsException(Throwable cause) {
        super(cause);
    }
}
