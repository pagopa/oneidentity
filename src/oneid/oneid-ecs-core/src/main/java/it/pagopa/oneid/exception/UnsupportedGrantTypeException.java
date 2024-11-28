package it.pagopa.oneid.exception;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class UnsupportedGrantTypeException extends RuntimeException {

  public static ErrorCode errorCode = ErrorCode.UNSUPPORTED_GRANT_TYPE_ERROR;
  private final String clientId;

  public UnsupportedGrantTypeException(String clientId) {
    super(OAuth2Error.UNSUPPORTED_GRANT_TYPE_CODE);
    this.clientId = clientId;
  }

}
