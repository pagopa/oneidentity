package it.pagopa.oneid.common.model.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class ClientNotFoundException extends AuthorizationErrorException {
  
  public ClientNotFoundException(String callbackUri, String state) {
    super(String.valueOf(ErrorCode.CLIENT_NOT_FOUND), callbackUri,
        ErrorCode.CLIENT_NOT_FOUND.getErrorMessage(), OAuth2Error.UNAUTHORIZED_CLIENT_CODE, state);
  }

  public ClientNotFoundException() {

  }

}
