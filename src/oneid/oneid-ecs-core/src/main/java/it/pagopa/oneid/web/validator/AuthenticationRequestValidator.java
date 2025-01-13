package it.pagopa.oneid.web.validator;

import static it.pagopa.oneid.web.controller.utils.MDCHandler.updateMDCClientAndStateProperties;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedGet;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedPost;
import it.pagopa.oneid.web.validator.annotations.AuthenticationRequestCheck;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;

public class AuthenticationRequestValidator implements
    ConstraintValidator<AuthenticationRequestCheck, Object> {

  @Inject
  Map<String, Client> clientsMap;

  @Override
  public void initialize(AuthenticationRequestCheck constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
    ResponseType responseType;
    String callbackUri;
    String clientId;
    String state;
    String idp;

    switch (o) {
      case AuthorizationRequestDTOExtendedGet authorizationRequestDTOExtendedGet -> {
        responseType = authorizationRequestDTOExtendedGet.getResponseType();
        callbackUri = authorizationRequestDTOExtendedGet.getRedirectUri();
        clientId = authorizationRequestDTOExtendedGet.getClientId();
        state = authorizationRequestDTOExtendedGet.getState();
        idp = authorizationRequestDTOExtendedGet.getIdp();
      }
      case AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost -> {
        responseType = authorizationRequestDTOExtendedPost.getResponseType();
        callbackUri = authorizationRequestDTOExtendedPost.getRedirectUri();
        clientId = authorizationRequestDTOExtendedPost.getClientId();
        state = authorizationRequestDTOExtendedPost.getState();
        idp = authorizationRequestDTOExtendedPost.getIdp();
      }
      default -> throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    if (responseType == null) {
      if (callbackUri != null && clientsMap.get(clientId) != null && clientsMap.get(clientId)
          .getCallbackURI().contains(callbackUri)) {
        throw new AuthorizationErrorException(
            ErrorCode.AUTHORIZATION_ERROR_RESPONSE_TYPE.getErrorMessage(), callbackUri, state,
            clientId);
      } else {
        throw new GenericHTMLException(ErrorCode.AUTHORIZATION_ERROR_RESPONSE_TYPE);
      }
    }
    if (idp == null) {
      if (callbackUri != null && clientsMap.get(clientId) != null && clientsMap.get(clientId)
          .getCallbackURI().contains(callbackUri)) {
        throw new AuthorizationErrorException(ErrorCode.AUTHORIZATION_ERROR_IDP.getErrorMessage(),
            callbackUri, state, clientId);
      } else {
        throw new GenericHTMLException(ErrorCode.AUTHORIZATION_ERROR_IDP);
      }
    }
    // Set MDC properties
    updateMDCClientAndStateProperties(clientId,
        state);
    return true;
  }
}


