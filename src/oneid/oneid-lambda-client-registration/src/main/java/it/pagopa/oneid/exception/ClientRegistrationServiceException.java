package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class ClientRegistrationServiceException extends RuntimeException {

  public ClientRegistrationServiceException(ErrorCode errorCode) {
    super(String.valueOf(errorCode));
  }
}
