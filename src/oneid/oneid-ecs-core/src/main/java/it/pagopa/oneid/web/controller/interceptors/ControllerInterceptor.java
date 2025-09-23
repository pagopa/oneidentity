package it.pagopa.oneid.web.controller.interceptors;

import static it.pagopa.oneid.web.controller.utils.MDCHandler.updateMDCClientAndStateProperties;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.enums.GrantType;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.exception.InvalidGrantException;
import it.pagopa.oneid.exception.InvalidRequestMalformedHeaderAuthorizationException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.exception.UnsupportedGrantTypeException;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.controller.utils.MDCHandler;
import it.pagopa.oneid.web.controller.utils.MDCProperty;
import it.pagopa.oneid.web.dto.SAMLResponseDTO;
import it.pagopa.oneid.web.dto.TokenRequestDTOExtended;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Interceptor
@ControllerCustomInterceptor
public class ControllerInterceptor {

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionServiceImpl;

  @Inject
  SAMLServiceImpl samlServiceImpl;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionService;

  @Inject
  CurrentAuthDTO currentAuthDTO;

  @AroundInvoke
  public Object handleControllerRequest(InvocationContext context) throws Exception {

    switch (context.getMethod().getName()) {

      case "samlACS" -> {
        handleACSRequest(context);
      }
      case "token" -> {
        handleTokenRequest(context);
      }
      default -> {
        return context.proceed();
      }
    }
    return context.proceed();

  }

  private void handleTokenRequest(InvocationContext context) {

    TokenRequestDTOExtended tokenRequestDTOExtended = null;

    for (Object o : context.getParameters()) {
      if (o instanceof TokenRequestDTOExtended) {
        tokenRequestDTOExtended = (TokenRequestDTOExtended) o;
      }
    }

    if (tokenRequestDTOExtended == null) {
      // TODO: consider collecting this as Client Error metric
      throw new InvalidRequestMalformedHeaderAuthorizationException();
    }

    String authorization = tokenRequestDTOExtended.getAuthorization().replaceAll("Basic ", "");
    byte[] decodedBytes;
    String decodedString;
    String clientId;
    String clientSecret;
    try {
      decodedBytes = Base64.getDecoder().decode(authorization);
      decodedString = new String(decodedBytes);
      clientId = URLDecoder.decode(decodedString.split(":")[0], StandardCharsets.UTF_8);
      clientSecret = URLDecoder.decode(decodedString.split(":")[1], StandardCharsets.UTF_8);
    } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
      // TODO: consider collecting this as Client Error metric
      throw new InvalidRequestMalformedHeaderAuthorizationException();
    }

    // TODO check if possible 5xx are returned as json or html which is an error
    if (!GrantType.AUTHORIZATION_CODE.equals(tokenRequestDTOExtended.getGrantType())) {
      Log.error("unsupported grant type: " + tokenRequestDTOExtended.getGrantType());
      throw new UnsupportedGrantTypeException(clientId);
    }

    oidcServiceImpl.authorizeClient(clientId, clientSecret);

    // Put Client ID into MDC {client.id} property
    MDCHandler.setMDCProperty(MDCProperty.CLIENT_ID, clientId);

    SAMLSession session;
    try {
      session = samlSessionServiceImpl.getSAMLSessionByCode(tokenRequestDTOExtended.getCode());
    } catch (SessionException e) {
      Log.error(
          "authorization code: " + tokenRequestDTOExtended.getCode() + " not found or expired");
      throw new InvalidGrantException(clientId);
    }

    // Put Client state into MDC {client.state} property
    MDCHandler.setMDCProperty(MDCProperty.CLIENT_STATE,
        session.getAuthorizationRequestDTOExtended().getState());

    currentAuthDTO.setSamlSession(session);

  }

  private void handleACSRequest(InvocationContext context) {

    SAMLResponseDTO samlResponseDTO = null;

    for (Object o : context.getParameters()) {
      if (o instanceof SAMLResponseDTO) {
        samlResponseDTO = (SAMLResponseDTO) o;
      }
    }

    if (samlResponseDTO == null) {
      // TODO: consider collecting this as IDP Error metric
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    org.opensaml.saml.saml2.core.Response response = null;
    try {
      response = samlServiceImpl.getSAMLResponseFromString(samlResponseDTO.getSAMLResponse());
    } catch (OneIdentityException e) {
      Log.error("error getting SAML Response");
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    // 1a. if in ResponseTo does not match with a pending AuthnRequest, raise an exception
    SAMLSession samlSession = null;
    String inResponseTo = response.getInResponseTo();

    if (inResponseTo == null || inResponseTo.isBlank()) {
      Log.error("inResponseTo parameter must not be null or blank");
      // TODO: consider collecting this as IDP Error metric
      throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }
    try {
      samlSession = samlSessionService.getSession(inResponseTo, RecordType.SAML);
    } catch (SessionException e) {
      Log.error("error during session management: " + e.getMessage());
      // TODO: consider collecting this as IDP Error metric
      throw new GenericHTMLException(ErrorCode.SESSION_ERROR);
    }

    // Set MDC properties
    updateMDCClientAndStateProperties(
        samlSession.getAuthorizationRequestDTOExtended().getClientId(),
        samlSession.getAuthorizationRequestDTOExtended().getState());

    currentAuthDTO.setResponse(response);
    currentAuthDTO.setSamlSession(samlSession);

  }
}
