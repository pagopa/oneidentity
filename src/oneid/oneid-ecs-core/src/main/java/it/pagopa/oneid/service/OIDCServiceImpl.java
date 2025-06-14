package it.pagopa.oneid.service;

import static it.pagopa.oneid.common.model.enums.Identifier.fiscalNumber;
import static it.pagopa.oneid.connector.utils.ConnectorConstants.VALID_TIME_ACCESS_TOKEN_MIN;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.connector.LastIDPUsedConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.LastIDPUsed;
import it.pagopa.oneid.common.model.dto.SecretDTO;
import it.pagopa.oneid.common.utils.HASHUtils;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.connector.KMSConnectorImpl;
import it.pagopa.oneid.exception.InvalidClientException;
import it.pagopa.oneid.exception.OIDCSignJWTException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.model.dto.JWKSSetDTO;
import it.pagopa.oneid.model.dto.JWKSUriMetadataDTO;
import it.pagopa.oneid.service.utils.OIDCUtils;
import it.pagopa.oneid.web.dto.TokenDataDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.w3c.dom.Element;
import software.amazon.awssdk.services.kms.model.GetPublicKeyResponse;

@ApplicationScoped
@CustomLogging
@Startup
public class OIDCServiceImpl implements OIDCService {

  private static final long LAST_IDP_USED_TTL = 730; // days

  @Inject
  @ConfigProperty(name = "kms_key_id")
  String KMS_KEY_ID;

  @ConfigProperty(name = "base_path")
  String BASE_PATH;

  @Inject
  OIDCUtils oidcUtils;
  @Inject
  KMSConnectorImpl kmsConnectorImpl;
  @Inject
  ClientConnectorImpl clientConnectorImpl;
  @Inject
  LastIDPUsedConnectorImpl lastIDPUsedConnectorImpl;
  @Inject
  Map<String, Client> clientsMap;
  @Inject
  MarshallerFactory marshallerFactory;

  private String getHashedIdFromAttributeDTOList(List<AttributeDTO> attributes) {
    for (AttributeDTO attribute : attributes) {
      if (fiscalNumber.name().equals(attribute.getAttributeName())) {
        return HASHUtils.generateIDHash(attribute.getAttributeValue());
      }
    }
    return null;
  }

  @Override
  public JWKSSetDTO getJWKSPublicKey() {
    GetPublicKeyResponse getPublicKeyResponse = kmsConnectorImpl.getPublicKey(KMS_KEY_ID);
    RSAPublicKey rsaPublicKey;
    try {
      rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
          .generatePublic(
              new X509EncodedKeySpec(getPublicKeyResponse.publicKey().asByteArray()));
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      Log.error("error during public key instantiation: "
          + e.getMessage());
      throw new RuntimeException(e);
    }

    return JWKSSetDTO.builder()
        .keyList(List.of(
            new JWKSUriMetadataDTO(getPublicKeyResponse.keyId().split("/")[1], rsaPublicKey)))
        .build();
  }

  @Override
  public OIDCProviderMetadata buildOIDCProviderMetadata() {
    Issuer issuer = new Issuer(BASE_PATH);
    URI jwksURI;
    URI authEndpointURI;
    URI tokenEndpointURI;
    try {
      jwksURI = new URI(BASE_PATH + "/oidc/keys");
      authEndpointURI = new URI(BASE_PATH + "/oidc/authorize");
      tokenEndpointURI = new URI(BASE_PATH + "/oidc/token");
    } catch (URISyntaxException e) {
      Log.error("error during endpoints URI creation: "
          + e.getMessage());
      throw new RuntimeException(e);
    }

    /* Required */
    OIDCProviderMetadata oidcProviderMetadata = new OIDCProviderMetadata(issuer,
        List.of(SubjectType.PUBLIC), jwksURI);

    oidcProviderMetadata.setAuthorizationEndpointURI(authEndpointURI);
    oidcProviderMetadata.setIDTokenJWSAlgs(List.of(JWSAlgorithm.RS256));
    oidcProviderMetadata.setResponseTypes(List.of(ResponseType.CODE));
    oidcProviderMetadata.setTokenEndpointURI(tokenEndpointURI);

    /* Not required */
    oidcProviderMetadata.setScopes(new Scope("openid"));
    oidcProviderMetadata.setGrantTypes(List.of(GrantType.AUTHORIZATION_CODE));
    oidcProviderMetadata.setTokenEndpointAuthMethods(
        List.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC));

    return oidcProviderMetadata;

  }

  @Override
  public AuthorizationRequest buildAuthorizationRequest(
      AuthorizationRequestDTO authorizationRequestDTO) {

    // The client identifier provisioned by the server
    ClientID clientID = new ClientID(authorizationRequestDTO.getClientId());

    // The requested scope values for the token
    Scope scope = new Scope(authorizationRequestDTO.getScope());

    // The client callback URI, typically pre-registered with the server
    URI callback = null;
    try {
      callback = new URI(authorizationRequestDTO.getRedirectUri());
    } catch (URISyntaxException e) {
      Log.error("error during setting of Callback URI: "
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
  public TokenDataDTO getOIDCTokens(String requestId, String clientId,
      List<AttributeDTO> attributeDTOList,
      String nonce, String entityId) {

    // Create access token
    // TODO is it ok for the 'scope' to be null?
    AccessToken accessToken = new BearerAccessToken(VALID_TIME_ACCESS_TOKEN_MIN * 60L, null);
    String id = null;

    //Create signed JWT ID token
    String signedJWTString;
    if (!clientsMap.get(clientId).isRequiredSameIdp()) {
      // if client does not need the "sameIdp" claim
      signedJWTString = oidcUtils.createSignedJWT(requestId, clientId, attributeDTOList,
          nonce);
    } else {
      // if client needs the "sameIdp" claim

      // Get hashed fiscalNumber from attribute list
      id = getHashedIdFromAttributeDTOList(attributeDTOList);
      if (id != null) {
        // if hashed fiscalNumber is present, use it as id for the findLastIDPUsed
        boolean sameIdp = false;
        Optional<LastIDPUsed> lastIDPUsed = lastIDPUsedConnectorImpl.findLastIDPUsed(id, clientId);
        if (lastIDPUsed.isPresent()) {
          // if there are last login information available for the id and clientId, check if tha lastIdp matches the current one
          // TODO do we need to check the 'ttl' parameter to avoid DynamoDB delayed deletion issues?
          sameIdp = Objects.equals(lastIDPUsed.get().getEntityId(), entityId);
        }
        if (!sameIdp) {
          // if the IDP has changed we need to update the lastIDP record
          long ttl = Instant.now().plus(LAST_IDP_USED_TTL, ChronoUnit.DAYS).getEpochSecond();
          lastIDPUsedConnectorImpl.updateLastIDPUsed(LastIDPUsed.builder()
              .id(id)
              .clientId(clientId)
              .entityId(entityId)
              .ttl(ttl)
              .build());
        }
        signedJWTString = oidcUtils.createSignedJWT(requestId, clientId, attributeDTOList,
            nonce, sameIdp);
      } else {
        // if hashed fiscalNumber is not present we can't check last login information
        signedJWTString = oidcUtils.createSignedJWT(requestId, clientId, attributeDTOList,
            nonce);
      }
    }
    SignedJWT signedJWTIDToken;
    try {
      signedJWTIDToken = SignedJWT.parse(signedJWTString);
    } catch (ParseException e) {
      Log.error("error during parsing JWT");
      throw new OIDCSignJWTException(e);
    }

    return TokenDataDTO
        .builder()
        .idToken(signedJWTIDToken.serialize())
        .idTokenType("openid")
        .accessToken(accessToken.toString())
        .tokenType(accessToken.getType().getValue())
        .expiresIn(accessToken.getLifetime())
        .scope("openid")
        .build();
  }

  @Override
  public void authorizeClient(String clientId, String clientSecret) {
    if (clientsMap.get(clientId) == null) {
      Log.debug("client not found");
      throw new InvalidClientException("Client ID not valid", clientId);
    }

    SecretDTO secretDTO = clientConnectorImpl.getClientSecret(clientId)
        .orElseThrow(() -> new InvalidClientException("Client secret not found", clientId));

    if (!HASHUtils.validateSecret(secretDTO.getSalt(), clientSecret,
        secretDTO.getSecret())) {
      Log.debug("client secret not valid");
      throw new InvalidClientException("Client secret not valid", clientId);
    }
  }

  @Override
  public String getStringValue(Element element) {
    StreamResult result = new StreamResult(new StringWriter());
    try {
      TransformerFactory
          .newInstance()
          .newTransformer()
          .transform(new DOMSource(element), result);
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
    return result.getWriter().toString();
  }

  @Override
  public Element getElementValueFromAuthnRequest(AuthnRequest authnRequest) {
    Marshaller out = marshallerFactory.getMarshaller(authnRequest);

    Element plaintextElement = null;
    try {
      plaintextElement = out.marshall(authnRequest);
    } catch (MarshallingException | NullPointerException e) {
      throw new RuntimeException(e);
    }

    return plaintextElement;
  }
}
