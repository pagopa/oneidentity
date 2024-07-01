package it.pagopa.oneid.service;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import it.pagopa.oneid.exception.OIDCAuthorizationException;
import it.pagopa.oneid.exception.OIDCSignJWTException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.model.dto.JWKSUriMetadataDTO;
import it.pagopa.oneid.web.dto.TokenDataDTO;
import java.util.List;

public interface OIDCService {

  JWKSUriMetadataDTO getJWSKPublicKey();

  OIDCProviderMetadata buildOIDCProviderMetadata();

  AuthorizationRequest buildAuthorizationRequest(AuthorizationRequestDTO authorizationRequestDTO);

  AuthorizationResponse getAuthorizationResponse(AuthorizationRequest authorizationRequest);

  TokenDataDTO getOIDCTokens(List<AttributeDTO> attributeDTOList, String nonce)
      throws OIDCSignJWTException;

  void authorizeClient(String clientId, String secret) throws OIDCAuthorizationException;

}
