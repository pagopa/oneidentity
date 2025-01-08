package it.pagopa.oneid.web.controller.interceptors;

import static it.pagopa.oneid.web.controller.utils.MDCHandler.updateMDCClientAndStateProperties;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.dto.SAMLResponseDTO;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@AcsCustomMDC
public class AcsCustomMDCInterceptor {

  @Inject
  SAMLServiceImpl samlServiceImpl;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionService;

  @Inject
  CurrentAuthDTO currentAuthDTO;

  @AroundInvoke
  public Object handleACSRequestMDC(InvocationContext context) throws Exception {

    SAMLResponseDTO samlResponseDTO = null;

    for (Object o : context.getParameters()) {
      if (o instanceof SAMLResponseDTO) {
        samlResponseDTO = (SAMLResponseDTO) o;
      }
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
    currentAuthDTO.setInResponseTo(inResponseTo);

    return context.proceed();
  }
}
