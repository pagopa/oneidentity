package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class IDPNotFoundException extends AuthorizationErrorException {

  public static final ErrorCode errorCode = ErrorCode.IDP_NOT_FOUND;
  private final String clientId;


  public IDPNotFoundException(String callbackUri, String state, String clientId) {
    super(String.valueOf(errorCode), callbackUri, OAuth2Error.INVALID_REQUEST_CODE,
        errorCode.getErrorMessage(), state);
    this.clientId = clientId;
  }
}
