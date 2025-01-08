package it.pagopa.oneid.web.controller.interceptors;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.enums.GrantType;
import it.pagopa.oneid.exception.InvalidGrantException;
import it.pagopa.oneid.exception.InvalidRequestMalformedHeaderAuthorizationException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.exception.UnsupportedGrantTypeException;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.controller.utils.MDCHandler;
import it.pagopa.oneid.web.controller.utils.MDCProperty;
import it.pagopa.oneid.web.dto.TokenRequestDTOExtended;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Interceptor
@TokenCustomMDC
public class TokenCustomMDCInterceptor {

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionServiceImpl;

  @Inject
  CurrentAuthDTO currentAuthDTO;

  @AroundInvoke
  public Object handleTokenRequestMDC(InvocationContext context) throws Exception {

    TokenRequestDTOExtended tokenRequestDTOExtended = null;

    for (Object o : context.getParameters()) {
      if (o instanceof TokenRequestDTOExtended) {
        tokenRequestDTOExtended = (TokenRequestDTOExtended) o;
      }
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
    if (!tokenRequestDTOExtended.getGrantType().equals(GrantType.AUTHORIZATION_CODE)) {
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
      throw new InvalidGrantException(clientId);
    }

    // Put Client state into MDC {client.state} property
    MDCHandler.setMDCProperty(MDCProperty.CLIENT_STATE,
        session.getAuthorizationRequestDTOExtended().getState());

    currentAuthDTO.setSamlSession(session);
    currentAuthDTO.setClientId(clientId);

    return context.proceed();
  }
}
