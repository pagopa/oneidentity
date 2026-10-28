package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class CieAgePolicyException extends RuntimeException {

  public enum Reason {
    BELOW_MINIMUM_AGE,
    ABOVE_MAXIMUM_AGE,
    PARENTAL_AUTHORIZATION_REQUIRED,
    INVALID_BIRTH_DATE,
    INVALID_CONFIGURATION
  }

  private final Reason reason;
  private final ErrorCode errorCode;
  private final String redirectUri;
  private final String state;
  private final String clientId;

  public CieAgePolicyException(Reason reason, String redirectUri, String state, String clientId) {
    super(reason.name());
    this.reason = reason;
    this.errorCode = reason == Reason.INVALID_BIRTH_DATE || reason == Reason.INVALID_CONFIGURATION
        ? ErrorCode.CIE_AGE_POLICY_NOT_VERIFIABLE
        : ErrorCode.CIE_AGE_POLICY_DENIED;
    this.redirectUri = redirectUri;
    this.state = state;
    this.clientId = clientId;
  }
}
