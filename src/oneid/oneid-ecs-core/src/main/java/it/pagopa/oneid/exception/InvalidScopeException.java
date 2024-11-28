package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidScopeException extends AuthorizationErrorException {

  public static final ErrorCode errorCode = ErrorCode.INVALID_SCOPE_ERROR;
  private final String clientId;

  public InvalidScopeException(String callbackUri, String state, String clientId) {
    super(String.valueOf(errorCode), callbackUri,
        errorCode.getErrorMessage(), OAuth2Error.INVALID_SCOPE_CODE,
        state);
    this.clientId = clientId;
  }
}
