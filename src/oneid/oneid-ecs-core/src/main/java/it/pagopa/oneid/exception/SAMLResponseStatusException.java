package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class SAMLResponseStatusException extends RuntimeException {

  private final String redirectUri;

  private final String idp;

  private final String clientId;

  private final String state;

  private final String errorCode;

  public SAMLResponseStatusException(ErrorCode errorCode, String redirectUri, String clientId,
      String idp, String state) {
    super(errorCode.getErrorMessage());
    this.errorCode = errorCode.getErrorCode();
    this.redirectUri = redirectUri;
    this.clientId = clientId;
    this.idp = idp;
    this.state = state;
  }
}
