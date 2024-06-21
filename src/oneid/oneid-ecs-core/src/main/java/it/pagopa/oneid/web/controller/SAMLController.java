package it.pagopa.oneid.web.controller;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.exception.OneIdentityException;
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
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Path(("/saml"))
public class SAMLController {

  @Inject
  SAMLServiceImpl samlServiceImpl;

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionSessionService;

  @Inject
  SessionServiceImpl<OIDCSession> oidcSessionSessionService;


  @POST
  @Path("/acs")
  public Response samlACS(@BeanParam @Valid SAMLResponseDTO samlResponseDTO)
      throws OneIdentityException {
    Log.info("[SAMLController.samlACS] start");

    org.opensaml.saml.saml2.core.Response response = samlServiceImpl.getSAMLResponseFromString(
        samlResponseDTO.getSAMLResponse());

    //1a. if in ResponseTo does not match with a pending AuthnRequest, raise an exception
    SAMLSession samlSession = samlSessionSessionService.getSession(response.getInResponseTo(),
        RecordType.SAML);

    // 1b. Check if Signatures are valid (Response and Assertion) and SubjectConfirmationData are correct
    samlServiceImpl.validateSAMLResponse(response,
        samlSession.getAuthorizationRequestDTOExtended().getIdp());

    // 2. Check status, will raise CustomException in case of error mapped to a custom html error page
    samlServiceImpl.checkSAMLStatus(response);

    // 4. Get Authorization Response

    AuthorizationRequest authorizationRequest = oidcServiceImpl.buildAuthorizationRequest(
        samlSession.getAuthorizationRequestDTOExtended());

    AuthorizationResponse authorizationResponse = oidcServiceImpl.getAuthorizationResponse(
        authorizationRequest);

    // 5. Update SAMLSession with SAMLResponse attribute
    samlSessionSessionService.setSAMLResponse(response.getInResponseTo(),
        samlResponseDTO.getSAMLResponse());

    // 6. Persist OIDC Session

    // Get the current time in epoch second format
    long creationTime = Instant.now().getEpochSecond();
    // Calculate the expiration time (2 days from now) in epoch second format
    long ttl = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();

    AuthorizationCode authorizationCode = authorizationResponse.toSuccessResponse()
        .getAuthorizationCode();

    OIDCSession oidcSession = new OIDCSession(response.getInResponseTo(), RecordType.OIDC,
        creationTime,
        ttl, authorizationCode.getValue());

    oidcSessionSessionService.saveSession(oidcSession);

    // 7. Redirect to client callback
    String clientCallbackUri = samlSession.getAuthorizationRequestDTOExtended().getRedirectUri();

    URI redirectStringResponse;
    try {
      redirectStringResponse = new URI(
          clientCallbackUri + "?code=" + authorizationCode + "&state="
              + authorizationResponse.getState());
    } catch (URISyntaxException e) {
      Log.error("[SAMLController.samlACS] error during setting of Callback URI: "
          + clientCallbackUri + "error: " + e.getMessage());
      throw new RuntimeException(e);
    }

    Log.info("[SAMLController.samlACS] end");

    return jakarta.ws.rs.core.Response
        .status(302)
        .location(redirectStringResponse)
        .build();
  }

  @GET
  @Path("/assertion")
  public Response assertion(@BeanParam @Valid AccessTokenDTO accessToken) {
    return Response.ok("Assertion Path").build();
  }

}
