package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class IDPNotFoundException extends AuthorizationErrorException {


  public IDPNotFoundException(String callbackUri, String state) {
    super(String.valueOf(ErrorCode.IDP_NOT_FOUND), callbackUri, state);
  }
}
