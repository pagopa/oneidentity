package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.RunOnVirtualThread;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import it.pagopa.oneid.exception.CallbackURINotFoundException;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.exception.IDPNotFoundException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.exception.InvalidGrantException;
import it.pagopa.oneid.exception.InvalidScopeException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.exception.UnsupportedResponseTypeException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.controller.interceptors.ControllerCustomInterceptor;
import it.pagopa.oneid.web.controller.interceptors.CurrentAuthDTO;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedGet;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedPost;
import it.pagopa.oneid.web.dto.TokenDataDTO;
import it.pagopa.oneid.web.dto.TokenRequestDTOExtended;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;

@Path(("/oidc"))
@Startup
@RunOnVirtualThread
public class OIDCController {

  @Inject
  SAMLServiceImpl samlServiceImpl;

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  CloudWatchConnectorImpl cloudWatchConnectorImpl;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionServiceImpl;

  @Inject
  SessionServiceImpl<AccessTokenSession> accessTokenSessionServiceImpl;

  @Inject
  Map<String, Client> clientsMap;

  @Inject
  CurrentAuthDTO currentAuthDTO;

  private <T> AuthorizationRequestDTOExtended getObject(T object) throws OneIdentityException {
    switch (object) {
      case AuthorizationRequestDTOExtendedGet authorizationRequestDTOExtendedGet -> {
        return AuthorizationRequestDTOExtended.builder()
            .idp(authorizationRequestDTOExtendedGet.getIdp())
            .clientId(authorizationRequestDTOExtendedGet.getClientId())
            .redirectUri(authorizationRequestDTOExtendedGet.getRedirectUri())
            .responseType(authorizationRequestDTOExtendedGet.getResponseType())
            .nonce(authorizationRequestDTOExtendedGet.getNonce())
            .scope(authorizationRequestDTOExtendedGet.getScope())
            .state(authorizationRequestDTOExtendedGet.getState())
            .ipAddress(authorizationRequestDTOExtendedGet.getIpAddress()).build();
      }
      case AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost -> {
        return AuthorizationRequestDTOExtended.builder()
            .idp(authorizationRequestDTOExtendedPost.getIdp())
            .clientId(authorizationRequestDTOExtendedPost.getClientId())
            .redirectUri(authorizationRequestDTOExtendedPost.getRedirectUri())
            .responseType(authorizationRequestDTOExtendedPost.getResponseType())
            .nonce(authorizationRequestDTOExtendedPost.getNonce())
            .scope(authorizationRequestDTOExtendedPost.getScope())
            .state(authorizationRequestDTOExtendedPost.getState())
            .ipAddress(authorizationRequestDTOExtendedPost.getIpAddress()).build();
      }
      default -> {
        throw new OneIdentityException("Invalid object for /oidc/authorize route");
      }
    }
  }

  @GET
  @Path("/keys")
  @Produces(MediaType.APPLICATION_JSON)
  public Response keys() {
    Log.debug("start");
    return Response.ok(oidcServiceImpl.getJWKSPublicKey()).build();
  }

  @POST
  @Path("/authorize")
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
  public Response authorizePost(
      @BeanParam @Valid AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost) {
    Log.debug("start");
    try {
      return handleAuthorize(getObject(authorizationRequestDTOExtendedPost));
    } catch (OneIdentityException e) {
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }
  }

  @GET
  @Path("/authorize")
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
  public Response authorizeGet(
      @BeanParam @Valid AuthorizationRequestDTOExtendedGet authorizationRequestDTOExtendedGet) {
    Log.debug("start");
    try {
      return handleAuthorize(getObject(authorizationRequestDTOExtendedGet));
    } catch (OneIdentityException e) {
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }
  }

  private Response handleAuthorize(
      AuthorizationRequestDTOExtended authorizationRequestDTOExtended) {
    Log.debug("start");
    Optional<IDP> idp;

    // 1. Check if clientId exists
    if (!clientsMap.containsKey(authorizationRequestDTOExtended.getClientId())) {
      Log.debug("selected Client not found");
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    // 1. Check if callbackUri exists among clientId parameters
    if (!clientsMap.get(authorizationRequestDTOExtended.getClientId()).getCallbackURI()
        .contains(authorizationRequestDTOExtended.getRedirectUri())) {
      Log.debug("redirect URI not found");
      throw new CallbackURINotFoundException(authorizationRequestDTOExtended.getClientId());
    }

    // 2. Check if idp exists
    idp = samlServiceImpl.getIDPFromEntityID(authorizationRequestDTOExtended.getIdp());
    if (idp.isEmpty()) {
      Log.debug("selected IDP not found");
      throw new IDPNotFoundException(authorizationRequestDTOExtended.getRedirectUri(),
          authorizationRequestDTOExtended.getState(),
          authorizationRequestDTOExtended.getClientId());
    }

    // 4. Check if scope is "openid"
    if (StringUtils.isBlank(authorizationRequestDTOExtended.getScope())
        || !authorizationRequestDTOExtended.getScope().equalsIgnoreCase("openid")) {
      Log.error("scope not supported");
      throw new InvalidScopeException(authorizationRequestDTOExtended.getRedirectUri(),
          authorizationRequestDTOExtended.getState(),
          authorizationRequestDTOExtended.getClientId());
    }

    // 5. Check if response type is "code"
    if (!authorizationRequestDTOExtended.getResponseType().equals(ResponseType.CODE)) {
      Log.error("response type not supported");
      throw new UnsupportedResponseTypeException(authorizationRequestDTOExtended.getRedirectUri(),
          authorizationRequestDTOExtended.getState(),
          authorizationRequestDTOExtended.getClientId());
    }

    Client client = clientsMap.get(authorizationRequestDTOExtended.getClientId());

    String idpSSOEndpoint = idp.get().getIdpSSOEndpoints()
        .get(SAMLConstants.SAML2_POST_BINDING_URI);

    // 6. Create SAML Authn Request using SAMLServiceImpl

    AuthnRequest authnRequest = null;
    try {
      authnRequest = samlServiceImpl.buildAuthnRequest(idpSSOEndpoint, client.getAcsIndex(),
          client.getAttributeIndex(), client.getAuthLevel().getValue());
    } catch (GenericAuthnRequestCreationException | IDPSSOEndpointNotFoundException |
             OneIdentityException e) {
      Log.error("error building authorization request: " + e.getMessage());
      throw new AuthorizationErrorException(authorizationRequestDTOExtended.getRedirectUri(),
          authorizationRequestDTOExtended.getState());
    }

    String encodedAuthnRequest = Base64.getEncoder().encodeToString(oidcServiceImpl.getStringValue(
        oidcServiceImpl.getElementValueFromAuthnRequest(authnRequest)).getBytes());
    String encodedRelayStateString = "";

    // 7. Persist SAMLSession

    // Get the current time in epoch second format
    long creationTime = authnRequest.getIssueInstant().getEpochSecond();

    // Calculate the expiration time (2 days from now) in epoch second format
    long ttl = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();

    SAMLSession samlSession = new SAMLSession(authnRequest.getID(), RecordType.SAML, creationTime,
        ttl, encodedAuthnRequest, authorizationRequestDTOExtended);

    try {
      samlSessionServiceImpl.saveSession(samlSession);
    } catch (SessionException e) {
      Log.error("error during session management " + e.getMessage());
      throw new AuthorizationErrorException(authorizationRequestDTOExtended.getRedirectUri(),
          authorizationRequestDTOExtended.getState());
    }

    String redirectAutoSubmitPOSTForm =
        "<form method='post' action=" + idpSSOEndpoint + " id='SAMLRequestForm'>"
            + "<input type='hidden' name='SAMLRequest' value=" + encodedAuthnRequest + " />"
            + "<input type='hidden' name='RelayState' value=" + encodedRelayStateString + " />"
            + "<input id='SAMLSubmitButton' type='submit' value='Submit' />" + "</form>"
            + "<script>document.getElementById('SAMLSubmitButton').style.visibility='hidden'; "
            + "document.getElementById('SAMLRequestForm').submit();</script>";

    return Response.ok(redirectAutoSubmitPOSTForm).type(MediaType.TEXT_HTML).build();
  }

  @POST
  @Path("/token")
  @Produces(MediaType.APPLICATION_JSON)
  @ControllerCustomInterceptor
  public TokenDataDTO token(@BeanParam @Valid TokenRequestDTOExtended tokenRequestDTOExtended)
      throws OneIdentityException {
    Log.debug("start");

    // 1. Get CurrentAuthDTO parameters
    SAMLSession session = currentAuthDTO.getSamlSession();
    String clientId = session.getAuthorizationRequestDTOExtended().getClientId();

    // check if redirect uri corresponds to session's redirect uri, needs to be mapped as InvalidGrantException
    if (!tokenRequestDTOExtended.getRedirectUri()
        .equals(session.getAuthorizationRequestDTOExtended().getRedirectUri())) {
      Log.error("provided redirect URI does not correspond to session redirect URI");
      throw new InvalidGrantException(clientId);
    }

    Assertion assertion = samlServiceImpl.getSAMLResponseFromString(session.getSAMLResponse())
        .getAssertions().getFirst();

    TokenDataDTO tokenDataDTO = oidcServiceImpl.getOIDCTokens(session.getSamlRequestID(), clientId,
        samlServiceImpl.getAttributesFromSAMLAssertion(assertion),
        session.getAuthorizationRequestDTOExtended().getNonce(),
        session.getAuthorizationRequestDTOExtended().getIdp());

    long creationTime = Instant.now().getEpochSecond();
    long ttl = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();

    AccessTokenSession accessTokenSession = new AccessTokenSession(session.getSamlRequestID(),
        RecordType.ACCESS_TOKEN, creationTime, ttl, tokenDataDTO.getAccessToken(),
        tokenDataDTO.getIdToken());

    try {
      accessTokenSessionServiceImpl.saveSession(accessTokenSession);
    } catch (SessionException e) {
      Log.error("error during session management " + e.getMessage());
      throw new InvalidGrantException(clientId);
    }

    cloudWatchConnectorImpl.sendClientSuccessMetricData(clientId);

    Log.debug("end");

    return tokenDataDTO;
  }

}
