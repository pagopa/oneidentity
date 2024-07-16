package it.pagopa.oneid.exception.mapper;


import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.SERVICE_PROVIDER_URI;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.FOUND;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.AssertionNotFoundException;
import it.pagopa.oneid.exception.AuthorizationErrorException;
import it.pagopa.oneid.exception.CallbackURINotFoundException;
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.exception.IDPNotFoundException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.exception.InvalidScopeException;
import it.pagopa.oneid.exception.OIDCAuthorizationException;
import it.pagopa.oneid.exception.OIDCSignJWTException;
import it.pagopa.oneid.exception.SAMLResponseStatusException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.exception.UnsupportedResponseTypeException;
import it.pagopa.oneid.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jetbrains.annotations.NotNull;

public class ExceptionMapper {

  private static @NotNull String getUri(String callbackUri, String errorCode,
      String errorMessage, String state) {
    String uri;
    if (state != null) {
      uri = callbackUri +
          "?error=" + errorCode + "&error_description=" + URLEncoder.encode(errorMessage,
          StandardCharsets.UTF_8)
          + "&state=" + state;
    } else {
      uri = callbackUri +
          "?error=" + errorCode + "&error_description=" + URLEncoder.encode(errorMessage,
          StandardCharsets.UTF_8);
    }
    return uri;
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapException(Exception exception) {
    Log.error("[ExceptionMapper.mapException]: " + ExceptionUtils.getStackTrace(
        exception));
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during execution.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  // TODO check this exception
  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapJakartaResourceNotFoundException(
      jakarta.ws.rs.NotFoundException jakartaResourceNotFoundException) {
    Log.error(
        "[ExceptionMapper.mapJakartaResourceNotFoundException]: "
            + jakartaResourceNotFoundException.getMessage());
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during execution.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapGenericHTMLException(GenericHTMLException genericHTMLException) {
    return genericHTMLError(genericHTMLException.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapSAMLResponseStatusException(
      SAMLResponseStatusException samlResponseStatusException) {
    return genericHTMLError(samlResponseStatusException.getMessage());
  }

  private RestResponse<Object> genericHTMLError(String errorCode) {
    try {
      return ResponseBuilder
          .create(FOUND)
          .location(new URI(SERVICE_PROVIDER_URI + "/login/error?errorCode=" + errorCode)).build();
    } catch (URISyntaxException e) {
      return ResponseBuilder.create(INTERNAL_SERVER_ERROR).build();
    }
  }

  private RestResponse<Object> authenticationErrorResponse(String callbackUri,
      String errorCode,
      String errorMessage,
      String state) {
    try {
      String uri = getUri(callbackUri, errorCode, errorMessage, state);
      return ResponseBuilder
          .create(FOUND)
          .location(
              new URI(uri))
          .build();
    } catch (URISyntaxException e) {
      Log.error("[ExceptionMapper.authenticationErrorResponse] invalid URI for redirecting: "
          + e.getMessage());
      return genericHTMLError(ErrorCode.AUTHORIZATION_ERROR.getErrorCode());
    }
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapSAMLValidationException(
      SAMLValidationException samlValidationException) {
    return genericHTMLError(samlValidationException.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapGenericAuthnRequestCreationException(
      GenericAuthnRequestCreationException genericAuthnRequestCreationException) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during generation of AuthnRequest.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapOIDCSignJWTException(
      OIDCSignJWTException oidcSignJWTException) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during signing of JWT.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapIDPSSOEndpointNotFoundException(
      IDPSSOEndpointNotFoundException idpssoEndpointNotFoundException) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "IDPSSO endpoint not found for selected idp.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapCallbackUriNotFoundException(
      CallbackURINotFoundException callbackURINotFoundException) {
    return genericHTMLError(callbackURINotFoundException.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapClientNotFoundException(
      ClientNotFoundException clientNotFoundException) {
    return authenticationErrorResponse(clientNotFoundException.getCallbackUri(),
        OAuth2Error.UNAUTHORIZED_CLIENT_CODE,
        ErrorCode.CLIENT_NOT_FOUND.getErrorMessage(),
        clientNotFoundException.getState());
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapInvalidScopeException(
      InvalidScopeException invalidScopeException) {
    return authenticationErrorResponse(invalidScopeException.getCallbackUri(),
        OAuth2Error.INVALID_SCOPE_CODE,
        ErrorCode.INVALID_SCOPE_ERROR.getErrorMessage(),
        invalidScopeException.getState());

  }

  @ServerExceptionMapper
  public RestResponse<Object> mapUnsupportedResponseTypeException(
      UnsupportedResponseTypeException unsupportedResponseTypeException) {
    return authenticationErrorResponse(unsupportedResponseTypeException.getCallbackUri(),
        OAuth2Error.UNSUPPORTED_RESPONSE_TYPE_CODE,
        ErrorCode.UNSUPPORTED_RESPONSE_TYPE_ERROR.getErrorMessage(),
        unsupportedResponseTypeException.getState());

  }

  @ServerExceptionMapper
  public RestResponse<Object> mapAuthorizationErrorException(
      AuthorizationErrorException authorizationErrorException) {
    return authenticationErrorResponse(authorizationErrorException.getCallbackUri(),
        OAuth2Error.SERVER_ERROR_CODE,
        ErrorCode.AUTHORIZATION_ERROR.getErrorMessage()
        , authorizationErrorException.getState());

  }

  @ServerExceptionMapper
  public RestResponse<Object> mapIDPNotFoundException(
      IDPNotFoundException idpNotFoundException) {
    return authenticationErrorResponse(idpNotFoundException.getCallbackUri(),
        OAuth2Error.INVALID_REQUEST_CODE,
        ErrorCode.IDP_NOT_FOUND.getErrorMessage(),
        idpNotFoundException.getState());
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapOIDCAuthorizationException(
      OIDCAuthorizationException oidcAuthorizationException) {
    Response.Status status = BAD_REQUEST;
    String message = "Error during OIDC authorization flow.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapAssertionNotFoundException(
      AssertionNotFoundException assertionNotFoundException) {
    Response.Status status = NOT_FOUND;
    String message = "Assertion not found.";
    return ResponseBuilder.create(status, buildErrorResponse(status, message)).build();
  }

  private ErrorResponse buildErrorResponse(Response.Status status, String message) {
    return ErrorResponse.builder()
        .title(status.getReasonPhrase())
        .status(status.getStatusCode())
        .detail(message)
        .build();
  }

}
