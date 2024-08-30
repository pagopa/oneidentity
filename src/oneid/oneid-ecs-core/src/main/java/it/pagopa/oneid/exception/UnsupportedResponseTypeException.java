package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class UnsupportedResponseTypeException extends AuthorizationErrorException {

  public UnsupportedResponseTypeException(String callbackUri, String state) {
    super(String.valueOf(ErrorCode.UNSUPPORTED_RESPONSE_TYPE_ERROR), callbackUri,
        ErrorCode.UNSUPPORTED_RESPONSE_TYPE_ERROR.getErrorMessage(),
        OAuth2Error.UNSUPPORTED_RESPONSE_TYPE_CODE, state);
  }
}