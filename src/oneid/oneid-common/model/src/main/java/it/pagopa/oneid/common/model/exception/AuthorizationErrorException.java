package it.pagopa.oneid.common.model.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class AuthorizationErrorException extends RuntimeException {

  private String callbackUri;
  private String OAuth2errorCode;
  private String errorMessage;
  private String state;
  public AuthorizationErrorException(String callbackUri, String state) {
    super(String.valueOf(ErrorCode.AUTHORIZATION_ERROR));
    this.callbackUri = callbackUri;
    this.OAuth2errorCode = OAuth2Error.SERVER_ERROR_CODE;
    this.errorMessage = ErrorCode.AUTHORIZATION_ERROR.getErrorMessage();
    this.state = state;
  }

  public AuthorizationErrorException(String errorCode, String callbackUri, String state) {
    super(errorCode);
    this.callbackUri = callbackUri;
    this.OAuth2errorCode = OAuth2Error.SERVER_ERROR_CODE;
    this.errorMessage = ErrorCode.AUTHORIZATION_ERROR.getErrorMessage();
    this.state = state;
  }


  public AuthorizationErrorException(String errorCode, String callbackUri, String OAuth2errorCode,
      String errorMessage, String state) {
    super(errorCode);
    this.callbackUri = callbackUri;
    this.OAuth2errorCode = OAuth2errorCode;
    this.errorMessage = errorMessage;
    this.state = state;
  }

  public AuthorizationErrorException() {
  }

  public void setOAuth2errorCode(String OAuth2errorCode) {
    this.OAuth2errorCode = OAuth2errorCode;
  }
}
