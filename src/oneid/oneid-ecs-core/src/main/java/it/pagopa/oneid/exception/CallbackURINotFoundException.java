package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;


@Getter
public class CallbackURINotFoundException extends RuntimeException {

  public static final ErrorCode errorCode = ErrorCode.CALLBACK_URI_NOT_FOUND;
  private final String clientId;

  public CallbackURINotFoundException(String clientId) {
    super(String.valueOf(errorCode));
    this.clientId = clientId;
  }

}
