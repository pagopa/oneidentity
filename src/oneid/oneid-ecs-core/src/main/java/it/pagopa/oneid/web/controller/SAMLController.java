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
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import it.pagopa.oneid.exception.AssertionNotFoundException;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.OIDCSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.BrowserBindingService;
import it.pagopa.oneid.service.CieAgePolicyService;
import it.pagopa.oneid.service.SAMLErrorRedirectService;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.service.ClientLookupService;
import it.pagopa.oneid.web.controller.interceptors.ControllerCustomInterceptor;
import it.pagopa.oneid.web.controller.interceptors.CurrentAuthDTO;
import it.pagopa.oneid.web.dto.AccessTokenDTO;
import it.pagopa.oneid.web.dto.SAMLResponseDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
// TODO remove: temporary IO-only minor filter dependencies
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSString;
import it.pagopa.oneid.exception.CieAgePolicyException;
import it.pagopa.oneid.exception.CieAgePolicyException.Reason;

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
  ClientLookupService clientLookupService;

  @Inject
  CurrentAuthDTO currentAuthDTO;

  @Inject
  BrowserBindingService browserBindingService;

  @Inject
  SAMLErrorRedirectService samlErrorRedirectService;

  @Inject
  CieAgePolicyService cieAgePolicyService;

  // TODO remove: temporary IO environment identification.
  @ConfigProperty(name = "entity_id")
  String entityId;

  @Context
  HttpHeaders httpHeaders;

  @POST
  @Path("/acs")
  @ControllerCustomInterceptor
  public Response samlACS(@BeanParam @Valid SAMLResponseDTO samlResponseDTO) {
    Log.debug("start");

    // 1a. Get CurrentAuthDTO parameters

    org.opensaml.saml.saml2.core.Response response = currentAuthDTO.getResponse();
    SAMLSession samlSession = currentAuthDTO.getSamlSession();
    var authorizationRequestContext = samlSession.getAuthorizationRequestDTOExtended();

    if (browserBindingService.enabled()) {
      BrowserBindingService.Outcome outcome = browserBindingService.verify(
          samlSession, httpHeaders.getCookies());
      cloudWatchConnectorImpl.sendBrowserBindingMetricData(outcome.name());
      if (outcome != BrowserBindingService.Outcome.MATCHED) {
        Log.warn("Browser binding outcome=" + outcome + " mode=" + browserBindingService.mode());
      }
      if (browserBindingService.enforcing()
          && outcome != BrowserBindingService.Outcome.MATCHED
          && outcome != BrowserBindingService.Outcome.LEGACY) {
        var authorizationRequest = samlSession.getAuthorizationRequestDTOExtended();
        if (authorizationRequest != null) {
          var directRedirect = samlErrorRedirectService.resolveBrowserBindingRedirect(
              authorizationRequest.getClientId(), authorizationRequest.getRedirectUri(),
              authorizationRequest.getState());
          if (directRedirect.isPresent()) {
            return Response.status(302).location(directRedirect.get()).build();
          }
          throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR,
              authorizationRequest.getRedirectUri(), authorizationRequest.getState(),
              authorizationRequest.getClientId());
        }
        throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
      }
    }

    // 1b. Update SAMLSession with SAMLResponse attribute
    try {
      samlSessionService.setSAMLResponse(response.getInResponseTo(),
          samlResponseDTO.getSAMLResponse());
    } catch (SessionException e) {
      Log.error("error during session management: " + e.getMessage());
      // TODO: consider collecting this as IDP Error metric
      throw new GenericHTMLException(ErrorCode.SESSION_ERROR,
          authorizationRequestContext.getRedirectUri(), authorizationRequestContext.getState(),
          authorizationRequestContext.getClientId());
    }

    // 1d. Check status, will raise CustomException in case of error mapped to a
    // custom HTML error page
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
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR,
          authorizationRequestContext.getRedirectUri(), authorizationRequestContext.getState(),
          authorizationRequestContext.getClientId());
    }

    // 2. Check if Signatures are valid (Response and Assertion) and if SAML
    // Response is formally correct
    Client client = clientLookupService.getClientById(
        samlSession.getAuthorizationRequestDTOExtended().getClientId()).orElse(null);
    if (client == null) {
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    // 3. Check if the requested auth level and comparison type are satisfied
    if (StringUtils.isBlank(samlSession.getRequestedAuthLevel())) {
      throw new GenericHTMLException(ErrorCode.SESSION_ERROR,
          authorizationRequestContext.getRedirectUri(), authorizationRequestContext.getState(),
          authorizationRequestContext.getClientId());
    }
    AuthLevel effectiveAuthLevel = AuthLevel.authLevelFromValue(
        samlSession.getRequestedAuthLevel());
    if (effectiveAuthLevel == null) {
      throw new GenericHTMLException(ErrorCode.SESSION_ERROR);
    }
    samlServiceImpl.validateSAMLResponse(response,
        samlSession.getAuthorizationRequestDTOExtended().getIdp(), client.getRequestedParameters(),
        Instant.ofEpochSecond(samlSession.getCreationTime()), effectiveAuthLevel,
        samlSession.getAuthorizationRequestDTOExtended().getRedirectUri(),
        samlSession.getAuthorizationRequestDTOExtended().getState(),
        samlSession.getAuthorizationRequestDTOExtended().getClientId(),
        client.getEidasIndex());

    // TODO remove: temporary minor filter for IO UAT and production.
    validateIoMinorFilter(response, authorizationRequestContext);

    cieAgePolicyService.validate(response, client, authorizationRequestContext.getIdp(),
        authorizationRequestContext.getRedirectUri(), authorizationRequestContext.getState());

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
      throw new GenericHTMLException(ErrorCode.SESSION_ERROR,
          authorizationRequestContext.getRedirectUri(), authorizationRequestContext.getState(),
          authorizationRequestContext.getClientId());
    }

    String clientCallbackUri = samlSession.getAuthorizationRequestDTOExtended().getRedirectUri();

    URI redirectStringResponse;
    try {
      UriBuilder redirectUriBuilder = UriBuilder.fromUri(URI.create(clientCallbackUri))
          .replaceQueryParam("code", "{authorizationCode}")
          .replaceQueryParam("state", "{authorizationState}");
      Map<String, Object> values = new HashMap<>();
      values.put("authorizationCode", authorizationCode.getValue());
      values.put("authorizationState", String.valueOf(authorizationResponse.getState()));
      redirectStringResponse = redirectUriBuilder.buildFromMap(values);
    } catch (IllegalArgumentException e) {
      Log.error("error during setting of Callback URI: " + clientCallbackUri + "error: "
          + e.getMessage());
      Log.error("error during creation of Callback URI");
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR,
          authorizationRequestContext.getRedirectUri(), authorizationRequestContext.getState(),
          authorizationRequestContext.getClientId());
    }

    cloudWatchConnectorImpl.sendIDPSuccessMetricData(
        samlSession.getAuthorizationRequestDTOExtended().getIdp());

    Log.debug("end");

    // 5. Redirect to client callback URI
    Response.ResponseBuilder redirect = Response.status(302).location(redirectStringResponse);
    if (browserBindingService.enabled() && samlSession.getBrowserBindingDigest() != null) {
      redirect.header("Set-Cookie", browserBindingService.clear(samlSession));
    }
    return redirect.build();
  }

  // TODO remove: temporary IO environment minor filter.
  private void validateIoMinorFilter(org.opensaml.saml.saml2.core.Response response,
      AuthorizationRequestDTOExtended authorizationRequestContext) {
    if (!"https://uat.io.oneid.pagopa.it/pub-op-full".equals(entityId)
        && !"https://io.oneid.pagopa.it/pub-op-full".equals(entityId)) {
      return;
    }

    var error = new CieAgePolicyException(Reason.INVALID_BIRTH_DATE,
        authorizationRequestContext.getRedirectUri(), authorizationRequestContext.getState(),
        authorizationRequestContext.getClientId());
    var birthAttributes = response.getAssertions().stream()
        .flatMap(assertion -> assertion.getAttributeStatements().stream())
        .flatMap(statement -> statement.getAttributes().stream())
        .filter(attribute -> "dateOfBirth".equals(attribute.getName()))
        .toList();
    if (birthAttributes.size() != 1 || birthAttributes.getFirst().getAttributeValues().size() != 1) {
      Log.warn("IO age filter: missing or ambiguous date of birth");
      throw error;
    }
    XMLObject value = birthAttributes.getFirst().getAttributeValues().getFirst();
    String birthDateText;
    if (value.getOrderedChildren() != null && !value.getOrderedChildren().isEmpty()) {
      birthDateText = null;
    } else if (value instanceof XSString stringValue) {
      birthDateText = stringValue.getValue();
    } else if (value instanceof XSAny anyValue) {
      birthDateText = anyValue.getTextContent();
    } else {
      birthDateText = value.getDOM() == null ? null : value.getDOM().getTextContent();
    }
    LocalDate birthDate;
    try {
      if (birthDateText == null || !birthDateText.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
        Log.warn("IO age filter: invalid date of birth");
        throw error;
      }
      birthDate = LocalDate.parse(birthDateText);
    } catch (DateTimeParseException e) {
      Log.warn("IO age filter: invalid date of birth");
      throw error;
    }
    LocalDate today = LocalDate.now(ZoneId.of("Europe/Rome"));
    if (birthDate.getYear() == 0 || birthDate.isAfter(today)) {
      Log.warn("IO age filter: invalid date of birth");
      throw error;
    }
    if (Period.between(birthDate, today).getYears() < 18) {
      Log.warn("IO age filter: minor");
      throw new CieAgePolicyException(Reason.BELOW_MINIMUM_AGE,
          authorizationRequestContext.getRedirectUri(), authorizationRequestContext.getState(),
          authorizationRequestContext.getClientId());
    }
  }

  @GET
  @Path("/assertion")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
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
