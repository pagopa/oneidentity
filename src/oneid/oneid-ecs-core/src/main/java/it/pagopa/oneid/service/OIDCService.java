package it.pagopa.oneid.service;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.TokenResponse;
import it.pagopa.oneid.exception.OIDCAuthorizationException;
import it.pagopa.oneid.exception.OIDCSignJWTException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import java.util.List;

public interface OIDCService {

  AuthorizationRequest buildAuthorizationRequest(AuthorizationRequestDTO authorizationRequestDTO);

  AuthorizationResponse getAuthorizationResponse(AuthorizationRequest authorizationRequest);

  TokenResponse getTokenResponse(List<AttributeDTO> attributeDTOList, String nonce)
      throws OIDCSignJWTException;

  void authorizeClient(String clientId, String secret) throws OIDCAuthorizationException;

}
