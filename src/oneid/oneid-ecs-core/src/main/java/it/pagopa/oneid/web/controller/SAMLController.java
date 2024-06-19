package it.pagopa.oneid.web.controller;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import io.quarkus.logging.Log;
import it.pagopa.oneid.exception.OneIdentityException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.model.session.OIDCSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.dto.AccessTokenDTO;
import it.pagopa.oneid.web.dto.SAMLResponseDTO;
import it.pagopa.oneid.web.utils.WebUtils;
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
  OIDCServiceImpl oidcserviceImpl;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionSessionService;

  @Inject
  SessionServiceImpl<OIDCSession> oidcSessionSessionService;


  @POST
  @Path("/acs")
  public Response samlACS(@BeanParam @Valid SAMLResponseDTO samlResponseDTO)
      throws OneIdentityException {
    // TODO Remove mock and set @Valid param
    Log.info("[samlACS] start");

    // 1a. if in ResponseTo does not match with a pending AuthnRequest, raise an exception
    org.opensaml.saml.saml2.core.Response response = samlServiceImpl.getSAMLResponseFromString(
        samlResponseDTO.getSAMLResponse());
    samlSessionSessionService.getSession(response.getInResponseTo(), RecordType.SAML)
        .orElseThrow(SessionException::new);

    // 1b. Check if Signatures are valid (Response and Assertion) and SubjectConfirmationData are correct
    samlServiceImpl.validateSAMLResponse(response);

    // 2. Check status, will raise CustomException in case of error mapped to a custom html error page
    samlServiceImpl.checkSAMLStatus(response);

    // 3. Parse RelayState
    JsonObject relayState = WebUtils.getJSONRelayState(samlResponseDTO.getRelayState());

    // 4. Get Authorization Response

    AuthorizationRequest authorizationRequest = oidcserviceImpl.buildAuthorizationRequest(
        new AuthorizationRequestDTO(
            relayState.get("clientId").toString(),
            ResponseType.valueOf(relayState.get("response_type").toString()),
            relayState.get("redirect_uri").toString(),
            relayState.get("scope").toString(),
            relayState.get("nonce").toString(),
            relayState.get("state").toString()
        ));

    AuthorizationResponse authorizationResponse = oidcserviceImpl.getAuthorizationResponse(
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

    // get Nonce
    String nonce = relayState.get("nonce").toString();

    OIDCSession oidcSession = new OIDCSession(response.getInResponseTo(), RecordType.OIDC,
        creationTime,
        ttl, authorizationCode.getValue(), nonce);

    oidcSessionSessionService.saveSession(oidcSession);

    // 7. Redirect to client callback
    String clientCallbackUri = relayState.get("redirect_uri").toString();

    URI redirectStringResponse;
    try {
      redirectStringResponse = new URI(
          clientCallbackUri + "?code=" + authorizationCode + "&state="
              + authorizationResponse.getState());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    Log.info("[samlACS] end");

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
