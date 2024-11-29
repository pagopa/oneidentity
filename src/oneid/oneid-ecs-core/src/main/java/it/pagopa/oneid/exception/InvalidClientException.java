package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidClientException extends RuntimeException {

  public static final ErrorCode errorCode = ErrorCode.INVALID_CLIENT_ERROR;
  private final String errorMessage;
  private final String clientId;


  public InvalidClientException(String errorMessage, String clientId) {
    super(OAuth2Error.INVALID_CLIENT_CODE);
    this.errorMessage = errorMessage;
    this.clientId = clientId;
  }

}
