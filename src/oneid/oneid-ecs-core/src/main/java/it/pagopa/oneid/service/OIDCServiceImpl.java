package it.pagopa.oneid.service;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.dto.SecretDTO;
import it.pagopa.oneid.common.utils.HASHUtils;
import it.pagopa.oneid.exception.OIDCAuthorizationException;
import it.pagopa.oneid.exception.OIDCSignJWTException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.service.utils.OIDCUtils;
import it.pagopa.oneid.web.dto.TokenDataDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OIDCServiceImpl implements OIDCService {

  @Inject
  OIDCUtils oidcUtils;

  @Inject
  ClientConnectorImpl clientConnectorImpl;

  @Inject
  Map<String, Client> clientsMap;


  @Override
  public AuthorizationRequest buildAuthorizationRequest(
      AuthorizationRequestDTO authorizationRequestDTO) {

    Log.debug("[OIDCServiceImpl.buildAuthorizationRequest] start");
    // The client identifier provisioned by the server
    ClientID clientID = new ClientID(authorizationRequestDTO.getClientId());

    // The requested scope values for the token
    Scope scope = new Scope(authorizationRequestDTO.getScope());

    // The client callback URI, typically pre-registered with the server
    URI callback = null;
    try {
      callback = new URI(authorizationRequestDTO.getRedirectUri());
    } catch (URISyntaxException e) {
      Log.error("[OIDCServiceImpl.buildAuthorizationRequest] error during setting of Callback URI: "
          + authorizationRequestDTO.getRedirectUri() + "error: " + e.getMessage());
      throw new RuntimeException(e);
    }

    // Generate random state string for pairing the response to the request
    State stateObj = new State(authorizationRequestDTO.getState());

    // Build the request

    return new AuthorizationRequest.Builder(
        new ResponseType(authorizationRequestDTO.getResponseType().getValue()), clientID)
        .scope(scope)
        .state(stateObj)
        .redirectionURI(callback)
        .build();
  }

  @Override
  public AuthorizationResponse getAuthorizationResponse(AuthorizationRequest authorizationRequest) {
    Log.info("[OIDCServiceImpl.getAuthorizationResponse] start");
    // TODO lookup the client
    ClientID clientID = authorizationRequest.getClientID();

    // The client callback URL, must be registered in the server's database
    URI callback = authorizationRequest.getRedirectionURI();

    // The state, must be echoed back with the response
    State state = authorizationRequest.getState();

    // The requested scope
    // TODO how to handle this ?
    Scope scope = authorizationRequest.getScope();

    // Generate the response...

    AuthorizationCode code = new AuthorizationCode();

    return new AuthenticationSuccessResponse(callback, code, null, null, state, null, null);

  }

  @Override
  public TokenDataDTO getOIDCTokens(List<AttributeDTO> attributeDTOList, String nonce)
      throws OIDCSignJWTException {
    Log.debug(("[OIDCServiceImpl.getOIDCTokens] start"));

    // Create access token
    AccessToken accessToken = new BearerAccessToken();

    //Create signed JWT ID token
    String signedJWTString = oidcUtils.createSignedJWT(attributeDTOList, nonce);
    SignedJWT signedJWTIDToken;
    try {
      signedJWTIDToken = SignedJWT.parse(signedJWTString);
    } catch (ParseException e) {
      Log.error("[OIDCServiceImpl.getOIDCTokens] error during parsing JWT");
      throw new OIDCSignJWTException(e);
    }

    return TokenDataDTO
        .builder()
        .idToken(signedJWTIDToken.serialize())
        .idTokenType("openid")
        .accessToken(accessToken.toString())
        .tokenType(accessToken.getType().getValue())
        .expiresIn(String.valueOf(accessToken.getLifetime()))
        .scope("openid")
        .build();
  }

  @Override
  public void authorizeClient(String clientId, String clientSecret)
      throws OIDCAuthorizationException {
    Log.debug("[OIDCServiceImpl.authorizeClient] start");
    if (clientsMap.get(clientId) == null) {
      Log.debug("[OIDCServiceImpl.authorizeClient] client not found");
      throw new OIDCAuthorizationException();
    }

    SecretDTO secretDTO = clientConnectorImpl.getClientSecret(clientId)
        .orElseThrow(OIDCAuthorizationException::new);

    if (!HASHUtils.validateSecret(clientSecret, secretDTO.getSalt(), secretDTO.getSecret())) {
      Log.debug("[OIDCServiceImpl.authorizeClient] client secret not valid");
      throw new OIDCAuthorizationException();
    }
  }
}
