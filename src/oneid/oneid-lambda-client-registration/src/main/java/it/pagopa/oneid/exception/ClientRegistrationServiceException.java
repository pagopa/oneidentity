package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class ClientRegistrationServiceException extends RuntimeException {

  public ClientRegistrationServiceException() {
    super(String.valueOf(ErrorCode.CLIENT_UTILS_ERROR));
  }
}
