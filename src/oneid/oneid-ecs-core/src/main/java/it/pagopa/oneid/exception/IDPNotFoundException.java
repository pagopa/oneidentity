package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class IDPNotFoundException extends AuthorizationErrorException {


  public IDPNotFoundException(String callbackUri, String state) {
    super(String.valueOf(ErrorCode.IDP_NOT_FOUND), callbackUri,
        ErrorCode.IDP_NOT_FOUND.getErrorMessage(), OAuth2Error.INVALID_REQUEST_CODE, state);
  }
}
