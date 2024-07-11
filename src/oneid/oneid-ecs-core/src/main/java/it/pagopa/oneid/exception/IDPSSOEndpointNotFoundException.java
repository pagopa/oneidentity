package it.pagopa.oneid.exception;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;

public class IDPSSOEndpointNotFoundException extends RuntimeException {

  public IDPSSOEndpointNotFoundException() {
    super(String.valueOf(ErrorCode.IDP_SSO_ENDPOINT_NOT_FOUND));
  }
}
