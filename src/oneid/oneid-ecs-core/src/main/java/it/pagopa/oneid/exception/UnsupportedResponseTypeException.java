package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class UnsupportedResponseTypeException extends AuthorizationErrorException {


  public static final ErrorCode errorCode = ErrorCode.UNSUPPORTED_RESPONSE_TYPE_ERROR;
  private final String clientId;

  public UnsupportedResponseTypeException(String callbackUri, String state, String clientId) {
    super(String.valueOf(errorCode), callbackUri,
        errorCode.getErrorMessage(),
        OAuth2Error.UNSUPPORTED_RESPONSE_TYPE_CODE, state);
    this.clientId = clientId;
  }
}