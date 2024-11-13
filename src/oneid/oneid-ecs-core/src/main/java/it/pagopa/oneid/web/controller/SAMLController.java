package it.pagopa.oneid.web.controller;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.AssertionNotFoundException;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.OIDCSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.dto.AccessTokenDTO;
import it.pagopa.oneid.web.dto.SAMLResponseDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;

@Path(("/saml"))
@Startup
public class SAMLController {

  @Inject
  SAMLServiceImpl samlServiceImpl;

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionService;

  @Inject
  SessionServiceImpl<OIDCSession> oidcSessionService;

  @Inject
  SessionServiceImpl<AccessTokenSession> accessTokenSessionService;

  @Inject
  Map<String, Client> clientsMap;


  @POST
  @Path("/acs")
  public Response samlACS(@BeanParam @Valid SAMLResponseDTO samlResponseDTO) {
    Log.info("start");

    org.opensaml.saml.saml2.core.Response response = null;
    try {
      response = samlServiceImpl.getSAMLResponseFromString(
          samlResponseDTO.getSAMLResponse());
    } catch (OneIdentityException e) {
      Log.error("error getting SAML Response");
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    // 1a. if in ResponseTo does not match with a pending AuthnRequest, raise an exception
    SAMLSession samlSession = null;
    String inResponseTo = response.getInResponseTo();

    if (inResponseTo == null || inResponseTo.isBlank()) {
      Log.error("inResponseTo parameter must not be null or blank");
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }
    try {
      samlSession = samlSessionService.getSession(inResponseTo,
          RecordType.SAML);
    } catch (SessionException e) {
      Log.error("error during session management: " + e.getMessage());
      throw new GenericHTMLException(ErrorCode.SESSION_ERROR);
    }

    // 1b. Update SAMLSession with SAMLResponse attribute
    try {
      samlSessionService.setSAMLResponse(inResponseTo,
          samlResponseDTO.getSAMLResponse());
    } catch (SessionException e) {
      Log.error("error during session management: " + e.getMessage());
      throw new GenericHTMLException(ErrorCode.SESSION_ERROR);
    }

    // 1c. Check status, will raise CustomException in case of error mapped to a custom html error page
    try {
      samlServiceImpl.checkSAMLStatus(response);
    } catch (OneIdentityException e) {
      Log.error(
          "error during SAMLResponse status check: " + e.getMessage());
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    // 2. Check if Signatures are valid (Response and Assertion) and if SAML Response is formally correct
    Client client = clientsMap.get(samlSession.getAuthorizationRequestDTOExtended().getClientId());
    samlServiceImpl.validateSAMLResponse(response,
        samlSession.getAuthorizationRequestDTOExtended().getIdp(),
        client.getRequestedParameters(), Instant.ofEpochSecond(samlSession.getCreationTime()),
        client.getAuthLevel());

    // 3. Get Authorization Response
    AuthorizationRequest authorizationRequest = oidcServiceImpl.buildAuthorizationRequest(
        samlSession.getAuthorizationRequestDTOExtended());

    AuthorizationResponse authorizationResponse = oidcServiceImpl.getAuthorizationResponse(
        authorizationRequest);

    long creationTime = Instant.now().getEpochSecond();
    long ttl = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();

    AuthorizationCode authorizationCode = authorizationResponse.toSuccessResponse()
        .getAuthorizationCode();

    OIDCSession oidcSession = new OIDCSession(inResponseTo, RecordType.OIDC,
        creationTime,
        ttl, authorizationCode.getValue());

    // 4. Save OIDC session
    try {
      oidcSessionService.saveSession(oidcSession);
    } catch (SessionException e) {
      Log.error("error during session management: " + e.getMessage());
      throw new GenericHTMLException(ErrorCode.SESSION_ERROR);
    }

    String clientCallbackUri = samlSession.getAuthorizationRequestDTOExtended().getRedirectUri();

    URI redirectStringResponse;
    try {
      redirectStringResponse = new URI(
          clientCallbackUri + "?code=" + authorizationCode + "&state="
              + authorizationResponse.getState());
    } catch (URISyntaxException e) {
      Log.error("error during setting of Callback URI: "
          + clientCallbackUri + "error: " + e.getMessage());
      Log.error("error during creation of Callback URI");
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    Log.info("end");

    // 5. Redirect to client callback URI
    return jakarta.ws.rs.core.Response
        .status(302)
        .location(redirectStringResponse)
        .build();
  }

  @GET
  @Path("/assertion")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response assertion(@BeanParam @Valid AccessTokenDTO accessToken) {
    Log.info("start");
    String samlResponse = null;
    try {
      samlResponse = accessTokenSessionService.getSAMLResponseByCode(
          accessToken.getAccessToken());
    } catch (SessionException e) {
      Log.debug("error during session management: " + e.getMessage());
      throw new AssertionNotFoundException(e);
    }
    return Response.ok(Base64.getDecoder().decode(samlResponse)).build();
  }

}
