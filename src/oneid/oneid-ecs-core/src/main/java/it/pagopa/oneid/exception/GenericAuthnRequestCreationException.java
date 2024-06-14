package it.pagopa.oneid.exception;

import it.pagopa.oneid.exception.enums.ErrorCode;

public class GenericAuthnRequestCreationException extends OneIdentityException {

    public GenericAuthnRequestCreationException() {
        super(ErrorCode.GENERIC_AUTHN_REQUEST_CREATION);
    }

    public GenericAuthnRequestCreationException(String message) {
        super(message);
    }

    public GenericAuthnRequestCreationException(Throwable cause) {
        super(cause);
    }
}
