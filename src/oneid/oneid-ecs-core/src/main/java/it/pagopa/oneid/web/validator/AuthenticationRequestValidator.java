package it.pagopa.oneid.web.validator;

import static it.pagopa.oneid.web.controller.utils.MDCHandler.updateMDCClientAndStateProperties;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.service.ClientLookupService;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedGet;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedPost;
import it.pagopa.oneid.web.validator.annotations.AuthenticationRequestCheck;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import java.util.Optional;

public class AuthenticationRequestValidator implements
    ConstraintValidator<AuthenticationRequestCheck, Object> {

  private static final Pattern ASSERTION_REF_PATTERN = Pattern.compile("^(sha256|sha384|sha512)-[A-Za-z0-9\\-_]+$");
  private static final int ASSERTION_REF_MAX_LENGTH = 160;

  @Inject
  ClientLookupService clientLookupService;

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
    String assertionRef;
    String acrValues;

    switch (o) {
      case AuthorizationRequestDTOExtendedGet authorizationRequestDTOExtendedGet -> {
        responseType = authorizationRequestDTOExtendedGet.getResponseType();
        callbackUri = authorizationRequestDTOExtendedGet.getRedirectUri();
        clientId = authorizationRequestDTOExtendedGet.getClientId();
        state = authorizationRequestDTOExtendedGet.getState();
        idp = authorizationRequestDTOExtendedGet.getIdp();
        assertionRef = authorizationRequestDTOExtendedGet.getAssertionRef();
        acrValues = authorizationRequestDTOExtendedGet.getAcrValues();
      }
      case AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost -> {
        responseType = authorizationRequestDTOExtendedPost.getResponseType();
        callbackUri = authorizationRequestDTOExtendedPost.getRedirectUri();
        clientId = authorizationRequestDTOExtendedPost.getClientId();
        state = authorizationRequestDTOExtendedPost.getState();
        idp = authorizationRequestDTOExtendedPost.getIdp();
        assertionRef = authorizationRequestDTOExtendedPost.getAssertionRef();
        acrValues = authorizationRequestDTOExtendedPost.getAcrValues();
      }
      default -> throw new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR);
    }

    Optional<Client> client = clientLookupService.getClientById(clientId);
    boolean hasValidCallback = callbackUri != null
        && client.isPresent()
        && client.get().getCallbackURI() != null
        && client.get().getCallbackURI().contains(callbackUri);

    if (responseType == null) {
      if (hasValidCallback) {
        throw new AuthorizationErrorException(
            ErrorCode.AUTHORIZATION_ERROR_RESPONSE_TYPE.getErrorMessage(), callbackUri, state,
            clientId);
      } else {
        throw new GenericHTMLException(ErrorCode.AUTHORIZATION_ERROR_RESPONSE_TYPE);
      }
    }
    if (idp == null) {
      if (hasValidCallback) {
        throw new AuthorizationErrorException(ErrorCode.AUTHORIZATION_ERROR_IDP.getErrorMessage(),
            callbackUri, state, clientId);
      } else {
        throw new GenericHTMLException(ErrorCode.AUTHORIZATION_ERROR_IDP);
      }
    }
    if (state == null) {
      if (hasValidCallback) {
        throw new AuthorizationErrorException(ErrorCode.AUTHORIZATION_ERROR_STATE.getErrorMessage(),
            callbackUri, null, clientId);
      } else {
        throw new GenericHTMLException(ErrorCode.AUTHORIZATION_ERROR_STATE);
      }
    }
    if (assertionRef != null && !assertionRef.isBlank() && !isValidAssertionRefFormat(assertionRef)) {
      if (hasValidCallback) {
        throw new AuthorizationErrorException(
            ErrorCode.OI_ERROR_INVALID_ASSERTION_REF.getErrorMessage(),
            callbackUri,
            OAuth2Error.INVALID_REQUEST_CODE,
            ErrorCode.OI_ERROR_INVALID_ASSERTION_REF.getErrorMessage(),
            state);
      } else {
        throw new GenericHTMLException(ErrorCode.OI_ERROR_INVALID_ASSERTION_REF);
      }
    }
    if (acrValues != null && !acrValues.isBlank()) {
      // Check if acrValues contains spaces (multiple values are not accepted)
      if (acrValues.contains(" ")) {
        if (hasValidCallback) {
          throw new AuthorizationErrorException(ErrorCode.ACR_VALUES_ERROR.getErrorMessage(),
              callbackUri, OAuth2Error.INVALID_REQUEST_CODE,
              ErrorCode.ACR_VALUES_ERROR.getErrorMessage(), state);
        } else {
          throw new GenericHTMLException(ErrorCode.ACR_VALUES_ERROR);
        }
      }
      // Check if acrValues is a valid AuthLevel value (not null, not L1, and not lower than
      // the client default)
      AuthLevel requestedLevel = AuthLevel.authLevelFromValue(acrValues);
      if (requestedLevel == null || requestedLevel == AuthLevel.L1
          || (client.isPresent() && client.get().getAuthLevel() != null
          && requestedLevel.compareTo(client.get().getAuthLevel()) < 0)) {
        if (hasValidCallback) {
          throw new AuthorizationErrorException(ErrorCode.ACR_VALUES_ERROR.getErrorMessage(),
              callbackUri, OAuth2Error.INVALID_REQUEST_CODE,
              ErrorCode.ACR_VALUES_ERROR.getErrorMessage(), state);
        } else {
          throw new GenericHTMLException(ErrorCode.ACR_VALUES_ERROR);
        }
      }
    }

    // Set MDC properties
    updateMDCClientAndStateProperties(clientId,
        state);
    return true;
  }

  private static boolean isValidAssertionRefFormat(String assertionRef) {
    return assertionRef.length() <= ASSERTION_REF_MAX_LENGTH
        && ASSERTION_REF_PATTERN.matcher(assertionRef).matches();
  }
}
