package it.pagopa.oneid.web.controller;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.RunOnVirtualThread;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
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
import it.pagopa.oneid.web.controller.interceptors.ControllerCustomInterceptor;
import it.pagopa.oneid.web.controller.interceptors.CurrentAuthDTO;
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
@RunOnVirtualThread
public class SAMLController {

  @Inject
  SAMLServiceImpl samlServiceImpl;

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  CloudWatchConnectorImpl cloudWatchConnectorImpl;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionService;

  @Inject
  SessionServiceImpl<OIDCSession> oidcSessionService;

  @Inject
  SessionServiceImpl<AccessTokenSession> accessTokenSessionService;

  @Inject
  Map<String, Client> clientsMap;

  @Inject
  CurrentAuthDTO currentAuthDTO;

  @POST
  @Path("/acs")
  @ControllerCustomInterceptor
  public Response samlACS(@BeanParam @Valid SAMLResponseDTO samlResponseDTO) {
    Log.debug("start");

    // 1a. Get CurrentAuthDTO parameters

    org.opensaml.saml.saml2.core.Response response = currentAuthDTO.getResponse();
    SAMLSession samlSession = currentAuthDTO.getSamlSession();

    // 1b. Check if the SAMLResponse contains multiple signatures,
    // in this case the raw SAMLResponse is stored in SAML record, but the flow is interrupted
    if (currentAuthDTO.isResponseWithMultipleSignatures()) {
      Log.error("SAML Response contains multiple signatures");
      cloudWatchConnectorImpl.sendIDPErrorMetricData(
          samlSession.getAuthorizationRequestDTOExtended().getIdp(),
          ErrorCode.IDP_ERROR_MULTIPLE_SAMLRESPONSE_SIGNATURES_PRESENT);
      throw new GenericHTMLException(ErrorCode.IDP_ERROR_MULTIPLE_SAMLRESPONSE_SIGNATURES_PRESENT);
    }

    // 1c. Check status, will raise CustomException in case of error mapped to a custom html error page
    try {
      samlServiceImpl.checkSAMLStatus(response,
          samlSession.getAuthorizationRequestDTOExtended().getRedirectUri(),
          samlSession.getAuthorizationRequestDTOExtended().getClientId(),
          samlSession.getAuthorizationRequestDTOExtended().getIdp(),
          samlSession.getAuthorizationRequestDTOExtended().getState());
    } catch (OneIdentityException e) {
      Log.error("error during SAMLResponse status check: " + e.getMessage());
      cloudWatchConnectorImpl.sendIDPErrorMetricData(
          samlSession.getAuthorizationRequestDTOExtended().getIdp(),
          ErrorCode.SAML_RESPONSE_STATUS_ERROR);
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    // 2. Check if Signatures are valid (Response and Assertion) and if SAML Response is formally correct
    Client client = clientsMap.get(samlSession.getAuthorizationRequestDTOExtended().getClientId());
    samlServiceImpl.validateSAMLResponse(response,
        samlSession.getAuthorizationRequestDTOExtended().getIdp(), client.getRequestedParameters(),
        Instant.ofEpochSecond(samlSession.getCreationTime()), client.getAuthLevel(),
        samlSession.getAuthorizationRequestDTOExtended().getRedirectUri(),
        samlSession.getAuthorizationRequestDTOExtended().getState(),
        samlSession.getAuthorizationRequestDTOExtended().getClientId());

    // 3. Get Authorization Response
    AuthorizationRequest authorizationRequest = oidcServiceImpl.buildAuthorizationRequest(
        samlSession.getAuthorizationRequestDTOExtended());

    AuthorizationResponse authorizationResponse = oidcServiceImpl.getAuthorizationResponse(
        authorizationRequest);

    long creationTime = Instant.now().getEpochSecond();
    long ttl = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();

    AuthorizationCode authorizationCode = authorizationResponse.toSuccessResponse()
        .getAuthorizationCode();

    OIDCSession oidcSession = new OIDCSession(response.getInResponseTo(), RecordType.OIDC,
        creationTime, ttl,
        authorizationCode.getValue());

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
      redirectStringResponse = new URI(clientCallbackUri + "?code=" + authorizationCode + "&state="
          + authorizationResponse.getState());
    } catch (URISyntaxException e) {
      Log.error("error during setting of Callback URI: " + clientCallbackUri + "error: "
          + e.getMessage());
      Log.error("error during creation of Callback URI");
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    cloudWatchConnectorImpl.sendIDPSuccessMetricData(
        samlSession.getAuthorizationRequestDTOExtended().getIdp());

    Log.debug("end");

    // 5. Redirect to client callback URI
    return jakarta.ws.rs.core.Response.status(302).location(redirectStringResponse).build();
  }

  @GET
  @Path("/assertion")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response assertion(@BeanParam @Valid AccessTokenDTO accessToken) {
    Log.debug("start");
    String samlResponse = null;
    try {
      samlResponse = accessTokenSessionService.getSAMLResponseByCode(accessToken.getAccessToken());
    } catch (SessionException e) {
      Log.debug("error during session management: " + e.getMessage());
      throw new AssertionNotFoundException(e);
    }
    return Response.ok(Base64.getDecoder().decode(samlResponse)).build();
  }

}
