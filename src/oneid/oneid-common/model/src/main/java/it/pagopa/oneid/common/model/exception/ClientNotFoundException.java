package it.pagopa.oneid.common.model.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class ClientNotFoundException extends AuthorizationErrorException {


  public ClientNotFoundException(String callbackUri, String state) {
    super(String.valueOf(ErrorCode.CLIENT_NOT_FOUND), callbackUri, state);
  }

  public ClientNotFoundException() {

  }

}
