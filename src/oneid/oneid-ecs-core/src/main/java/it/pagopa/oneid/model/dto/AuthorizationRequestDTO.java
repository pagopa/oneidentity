package it.pagopa.oneid.model.dto;

import com.nimbusds.oauth2.sdk.ResponseType;

public class AuthorizationRequestDTO {
    ResponseType.Value responseType;

    String scopeType;

    String clientId;

    String state;

    String callbackUri;
}
