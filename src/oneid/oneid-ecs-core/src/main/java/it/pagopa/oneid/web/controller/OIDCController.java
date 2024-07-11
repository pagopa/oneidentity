package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.CallbackURINotFoundException;
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.exception.IDPNotFoundException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.exception.OIDCAuthorizationException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedGet;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedPost;
import it.pagopa.oneid.web.dto.TokenDataDTO;
import it.pagopa.oneid.web.dto.TokenRequestDTOExtended;
import it.pagopa.oneid.web.utils.WebUtils;
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
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

@Path(("/oidc"))
public class OIDCController {

  @Inject
  SAMLServiceImpl samlServiceImpl;

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionServiceImpl;

  @Inject
  SessionServiceImpl<AccessTokenSession> accessTokenSessionServiceImpl;

  @Inject
  Map<String, Client> clientsMap;

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
            .build();
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
            .build();
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
    Log.info("[OIDCController.keys] start");
    return Response.ok(oidcServiceImpl.getJWSKPublicKey()).build();
  }

  @POST
  @Path("/authorize")
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
  public Response authorizePost(
      @BeanParam @Valid AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost) {
    Log.info("[OIDCController.authorizePost] start");
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
    Log.info("[OIDCController.authorizeGet] start");
    try {
      return handleAuthorize(getObject(authorizationRequestDTOExtendedGet));
    } catch (OneIdentityException e) {
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }
  }

  private Response handleAuthorize(
      AuthorizationRequestDTOExtended authorizationRequestDTOExtended) {
    // TODO do we need to support ResponseMode?
    Log.info("[OIDCController.handleAuthorize] start");
    Optional<EntityDescriptor> idp;
    // 1. Check if idp exists
    try {
      idp = samlServiceImpl.getEntityDescriptorFromEntityID(
          authorizationRequestDTOExtended.getIdp());
      if (idp.isEmpty()) {
        Log.debug("[OIDCController.handleAuthorize] selected IDP not found");
        throw new IDPNotFoundException();
      }
    } catch (OneIdentityException e) {
      Log.error(
          "[OIDCController.handleAuthorize] error getting EntityDescriptor from provided EntityID");
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    // 2. Check if clientId exists
    if (!clientsMap.containsKey(authorizationRequestDTOExtended.getClientId())) {
      Log.debug("[OIDCController.handleAuthorize] selected Client not found");
      throw new ClientNotFoundException();
    }

    // 3. Check if callbackUri exists among clientId parameters
    if (!clientsMap.get(authorizationRequestDTOExtended.getClientId()).getCallbackURI()
        .contains(authorizationRequestDTOExtended.getRedirectUri())) {
      Log.debug("[OIDCController.handleAuthorize] redirect URI not found");
      throw new CallbackURINotFoundException();
    }

    Client client = clientsMap.get(authorizationRequestDTOExtended.getClientId());

    // TODO is it correct to retrieve the 0 indexed?
    String idpSSOEndpoint = idp.get().getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol")
        .getSingleSignOnServices().getFirst().getLocation();

    // 4. Create SAML Authn Request using SAMLServiceImpl

    AuthnRequest authnRequest = null;
    try {
      authnRequest = samlServiceImpl.buildAuthnRequest(
          authorizationRequestDTOExtended.getIdp(), client.getAcsIndex(),
          client.getAttributeIndex(),
          client.getAuthLevel());
    } catch (GenericAuthnRequestCreationException | IDPSSOEndpointNotFoundException |
             OneIdentityException e) {
      Log.error("[OIDCController.handleAuthorize] error building authorization request: "
          + e.getMessage());
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    String encodedAuthnRequest = Base64.getEncoder().encodeToString(
        WebUtils.getStringValue(WebUtils.getElementValueFromAuthnRequest(authnRequest)).getBytes());
    String encodedRelayStateString = "";

    // 5. Persist SAMLSession

    // Get the current time in epoch second format
    long creationTime = Instant.now().getEpochSecond();

    // Calculate the expiration time (2 days from now) in epoch second format
    long ttl = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();

    SAMLSession samlSession = new SAMLSession(authnRequest.getID(), RecordType.SAML, creationTime,
        ttl, encodedAuthnRequest, authorizationRequestDTOExtended);

    try {
      samlSessionServiceImpl.saveSession(samlSession);
    } catch (SessionException e) {
      Log.error("[OIDCController.handleAuthorize] error during session management "
          + e.getMessage());
      throw new GenericHTMLException(ErrorCode.SESSION_ERROR);
    }

    String redirectAutoSubmitPOSTForm = "<form method='post' action=" + idpSSOEndpoint
        + " id='SAMLRequestForm'>" +
        "<input type='hidden' name='SAMLRequest' value=" + encodedAuthnRequest + " />" +
        "<input type='hidden' name='RelayState' value=" + encodedRelayStateString + " />" +
        "<input id='SAMLSubmitButton' type='submit' value='Submit' />" +
        "</form>" +
        "<script>document.getElementById('SAMLSubmitButton').style.visibility='hidden'; " +
        "document.getElementById('SAMLRequestForm').submit();</script>";

    return Response
        .ok(redirectAutoSubmitPOSTForm)
        .type(MediaType.TEXT_HTML)
        .build();
  }

  @POST
  @Path("/token")
  @Produces(MediaType.APPLICATION_JSON)
  public TokenDataDTO token(@BeanParam @Valid TokenRequestDTOExtended tokenRequestDTOExtended)
      throws OneIdentityException {
    Log.info("[OIDCController.token] start");

    String authorization = tokenRequestDTOExtended.getAuthorization().replaceAll("Basic ", "");

    byte[] decodedBytes = Base64.getDecoder().decode(authorization);
    String decodedString = new String(decodedBytes);

    String clientId = decodedString.split(":")[0];
    String secret = decodedString.split(":")[1];

    oidcServiceImpl.authorizeClient(clientId, secret);

    SAMLSession session;
    try {
      session = samlSessionServiceImpl.getSAMLSessionByCode(
          tokenRequestDTOExtended.getCode());
    } catch (SessionException e) {
      throw new OIDCAuthorizationException(e);
    }

    Assertion assertion = samlServiceImpl.getSAMLResponseFromString(session.getSAMLResponse())
        .getAssertions()
        .getFirst();

    TokenDataDTO tokenDataDTO = oidcServiceImpl.getOIDCTokens(
        samlServiceImpl.getAttributesFromSAMLAssertion(assertion),
        session.getAuthorizationRequestDTOExtended().getNonce());

    long creationTime = Instant.now().getEpochSecond();
    long ttl = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();

    AccessTokenSession accessTokenSession = new AccessTokenSession(session.getSamlRequestID(),
        RecordType.ACCESS_TOKEN,
        creationTime,
        ttl, tokenDataDTO.getAccessToken(), tokenDataDTO.getIdToken());

    accessTokenSessionServiceImpl.saveSession(accessTokenSession);

    Log.debug("[OIDCController.token] end");

    return tokenDataDTO;
  }

}
