package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.exception.CallbackURINotFoundException;
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.IDPNotFoundException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
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
  @Path("/.well-known/openid-configuration")
  @Produces(MediaType.APPLICATION_JSON)
  public Response openIDConfig() {
    return Response.ok(oidcServiceImpl.buildOIDCProviderMetadata().toJSONObject()).build();
  }

  @GET
  @Path("/keys")
  @Produces(MediaType.APPLICATION_JSON)
  public Response keys() {
    return Response.ok(oidcServiceImpl.getJWSKPublicKey()).build();
  }

  @POST
  @Path("/authorize")
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
  public Response authorizePost(
      @BeanParam @Valid AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost)
      throws OneIdentityException {
    return handleAuthorize(getObject(authorizationRequestDTOExtendedPost));
  }

  @GET
  @Path("/authorize")
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
  public Response authorizeGet(
      @BeanParam @Valid AuthorizationRequestDTOExtendedGet authorizationRequestDTOExtendedGet)
      throws OneIdentityException {
    return handleAuthorize(getObject(authorizationRequestDTOExtendedGet));
  }

  private Response handleAuthorize(AuthorizationRequestDTOExtended authorizationRequestDTOExtended)
      throws SAMLUtilsException, IDPNotFoundException, ClientNotFoundException, CallbackURINotFoundException, GenericAuthnRequestCreationException, IDPSSOEndpointNotFoundException, SessionException {
    // TODO setup logging utility

    // 1. Check if idp exists
    if (samlServiceImpl.getEntityDescriptorFromEntityID(authorizationRequestDTOExtended.getIdp())
        .isEmpty()) {
      throw new IDPNotFoundException();
    }

    // 2. Check if clientId exists
    if (!clientsMap.containsKey(authorizationRequestDTOExtended.getClientId())) {
      throw new ClientNotFoundException();
    }

    // 3. Check if callbackUri exists among clientId parameters
    if (!clientsMap.get(authorizationRequestDTOExtended.getClientId()).getCallbackURI()
        .contains(authorizationRequestDTOExtended.getRedirectUri())) {
      throw new CallbackURINotFoundException();
    }

    EntityDescriptor idp = samlServiceImpl.getEntityDescriptorFromEntityID(
        authorizationRequestDTOExtended.getIdp()).get();
    Client client = clientsMap.get(authorizationRequestDTOExtended.getClientId());

    // TODO is it correct to retrieve the 0 indexed?
    String idpSSOEndpoint = idp.getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol")
        .getSingleSignOnServices().getFirst().getLocation();

    // 4. Create SAML Authn Request using SAMLServiceImpl

    // TODO who owns spid level?
    AuthnRequest authnRequest = samlServiceImpl.buildAuthnRequest(
        authorizationRequestDTOExtended.getIdp(), client.getAcsIndex(), client.getAttributeIndex(),
        client.getAuthLevel());

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
    samlSessionServiceImpl.saveSession(samlSession);

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
    Log.debug("[OIDCController.token] start");

    String authorization = tokenRequestDTOExtended.getAuthorization().replaceAll("Basic ", "");

    byte[] decodedBytes = Base64.getDecoder().decode(authorization);
    String decodedString = new String(decodedBytes);

    String clientId = decodedString.split(":")[0];
    String secret = decodedString.split(":")[1];

    oidcServiceImpl.authorizeClient(clientId, secret);

    SAMLSession session = samlSessionServiceImpl.getSAMLSessionByCode(
        tokenRequestDTOExtended.getCode());

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
