package it.pagopa.oneid.exception;

import it.pagopa.oneid.exception.enums.ErrorCode;

public class IDPSSOEndpointNotFoundException extends OneIdentityException {

  public IDPSSOEndpointNotFoundException() {
    super(ErrorCode.IDPSSOENDPOINT_NOT_FOUND);
  }

  public IDPSSOEndpointNotFoundException(String customMessage) {
    super(customMessage);
  }

  public IDPSSOEndpointNotFoundException(Throwable cause) {
    super(cause);
  }
}
