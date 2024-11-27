package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidGrantException extends RuntimeException {

  public static ErrorCode errorCode = ErrorCode.INVALID_GRANT_ERROR;
  private final String clientId;

  public InvalidGrantException(String clientId) {
    super(OAuth2Error.INVALID_GRANT_CODE);
    this.clientId = clientId;
  }

}
