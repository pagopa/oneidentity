package it.pagopa.oneid.exception;

import it.pagopa.oneid.exception.enums.ErrorCode;

public class CallbackURINotFoundException extends OneIdentityException {

    public CallbackURINotFoundException() {
        super(ErrorCode.CALLBACK_URINOT_FOUND);
    }

    public CallbackURINotFoundException(String customErrorMessage) {
        super(customErrorMessage);
    }

    public CallbackURINotFoundException(Throwable cause) {
        super(cause);
    }
}
