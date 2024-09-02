package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class InvalidScopeException extends AuthorizationErrorException {

  public InvalidScopeException(String callbackUri, String state) {
    super(String.valueOf(ErrorCode.INVALID_SCOPE_ERROR), callbackUri,
        ErrorCode.INVALID_SCOPE_ERROR.getErrorMessage(), OAuth2Error.INVALID_SCOPE_CODE,
        state);
  }
}
