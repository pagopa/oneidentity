package it.pagopa.oneid.service;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.TokenResponse;
import it.pagopa.oneid.common.Client;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTO;

import java.util.List;

public interface OIDCService {

    Client getClientRegistration(String clientID);

    AuthorizationRequest buildAuthorizationRequest(AuthorizationRequestDTO authorizationRequestDTO);

    AuthorizationResponse getAuthorizationResponse(AuthorizationRequest authorizationRequest);

    TokenResponse getTokenResponse(List<AttributeDTO<?>> attributeDTO);

}
