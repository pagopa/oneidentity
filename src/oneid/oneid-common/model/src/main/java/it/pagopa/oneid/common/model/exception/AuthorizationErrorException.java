package it.pagopa.oneid.common.model.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AuthorizationErrorException extends RuntimeException {

  public static final ErrorCode errorCode = ErrorCode.AUTHORIZATION_ERROR;
  private String callbackUri;
  @Setter
  private String OAuth2errorCode;
  private String errorMessage;
  private String state;
  private String clientId;

  public AuthorizationErrorException(String callbackUri, String state) {
    super(String.valueOf(errorCode));
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

  public AuthorizationErrorException(String errorCode, String callbackUri, String state,
      String clientId) {
    super(errorCode);
    this.callbackUri = callbackUri;
    this.OAuth2errorCode = OAuth2Error.SERVER_ERROR_CODE;
    this.errorMessage = ErrorCode.AUTHORIZATION_ERROR.getErrorMessage();
    this.state = state;
    this.clientId = clientId;
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

}
