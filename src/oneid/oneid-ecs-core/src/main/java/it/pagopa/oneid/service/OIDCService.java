package it.pagopa.oneid.service;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.model.dto.JWKSSetDTO;
import it.pagopa.oneid.web.dto.TokenDataDTO;
import java.util.List;

public interface OIDCService {

  JWKSSetDTO getJWSKPublicKey();

  OIDCProviderMetadata buildOIDCProviderMetadata();

  AuthorizationRequest buildAuthorizationRequest(AuthorizationRequestDTO authorizationRequestDTO);

  AuthorizationResponse getAuthorizationResponse(AuthorizationRequest authorizationRequest);

  TokenDataDTO getOIDCTokens(String requestId, String clientId, List<AttributeDTO> attributeDTOList,
      String nonce);

  void authorizeClient(String clientId, String encodedSecret);

}
