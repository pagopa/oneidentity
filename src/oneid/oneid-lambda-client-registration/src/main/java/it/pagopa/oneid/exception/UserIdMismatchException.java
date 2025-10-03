package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class UserIdMismatchException extends RuntimeException {

  public UserIdMismatchException() {
    super(String.valueOf(ErrorCode.USER_ID_MISMATCH_ERROR.getErrorMessage()));
  }
}
