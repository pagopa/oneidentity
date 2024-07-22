package it.pagopa.oneid.exception.mapper;


import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.SERVICE_PROVIDER_URI;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.FOUND;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport.Violation;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.AssertionNotFoundException;
import it.pagopa.oneid.exception.CallbackURINotFoundException;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.exception.IDPNotFoundException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.exception.InvalidClientException;
import it.pagopa.oneid.exception.InvalidGrantException;
import it.pagopa.oneid.exception.InvalidRequestMalformedHeaderAuthorizationException;
import it.pagopa.oneid.exception.InvalidScopeException;
import it.pagopa.oneid.exception.OIDCAuthorizationException;
import it.pagopa.oneid.exception.OIDCSignJWTException;
import it.pagopa.oneid.exception.SAMLResponseStatusException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.exception.UnsupportedGrantTypeException;
import it.pagopa.oneid.exception.UnsupportedResponseTypeException;
import it.pagopa.oneid.model.ErrorResponse;
import it.pagopa.oneid.web.dto.TokenRequestErrorDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.Path.Node;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMapper {

  private static final String VALIDATION_HEADER = "validation-exception";

  private static String getUri(String callbackUri, String errorCode,
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
    Log.error(ExceptionUtils.getStackTrace(
        exception));
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during execution.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  // TODO check this exception
  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapJakartaResourceNotFoundException(
      jakarta.ws.rs.NotFoundException jakartaResourceNotFoundException) {
    Log.error(jakartaResourceNotFoundException.getMessage());
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during execution.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapGenericHTMLException(GenericHTMLException genericHTMLException) {
    return genericHTMLError(genericHTMLException.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapValidationException(ValidationException validationException) {
    if (validationException.getCause() instanceof AuthorizationErrorException authorizationErrorException) {
      Log.error("AuthorizationErrorException encountered");
      return authenticationErrorResponse(authorizationErrorException.getCallbackUri(),
          OAuth2Error.INVALID_REQUEST_CODE, authorizationErrorException.getMessage(),
          authorizationErrorException.getState());
    } else if (validationException.getCause() instanceof GenericHTMLException genericHTMLException) {
      Log.error("GenericHTMLException encountered");
      return genericHTMLError(genericHTMLException.getMessage());
    }
    if (validationException.getMessage().contains("authorizeGet.arg0.clientId")
        || validationException.getMessage().contains("authorizePost.arg0.clientId")) {
      Log.error("Client ID not specified");
      return genericHTMLError(ErrorCode.GENERIC_HTML_ERROR.getErrorMessage());
    }
    if (validationException.getMessage().contains("token.arg0.")) {
      Log.error("Token request not correct");
      String message = "The request is missing a required parameter.";
      return RestResponse.status(BAD_REQUEST,
          buildTokenRequestErrorDTO(OAuth2Error.INVALID_REQUEST_CODE, message));
    }
    // TODO add before this 'if' other cases that must be mapped explicitly
    if (!(validationException instanceof ResteasyReactiveViolationException resteasyViolationException)) {
      // Not a violation in a REST endpoint call, but rather in an internal component.
      // This is an internal error: handle through the QuarkusErrorHandler,
      // which will return HTTP status 500 and log the exception.
      Log.error(validationException.getMessage());
      throw validationException;
    }
    if (hasReturnValueViolation(resteasyViolationException.getConstraintViolations())) {
      // This is an internal error: handle through the QuarkusErrorHandler,
      // which will return HTTP status 500 and log the exception.
      Log.error(validationException.getMessage());
      throw resteasyViolationException;
    }
    return buildViolationReportResponse(resteasyViolationException);

  }

  @ServerExceptionMapper
  public RestResponse<Object> mapSAMLResponseStatusException(
      SAMLResponseStatusException samlResponseStatusException) {
    return genericHTMLError(samlResponseStatusException.getMessage());
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
    return ResponseBuilder.create(status, buildErrorResponse(status, message))
        .type(MediaType.APPLICATION_JSON_TYPE).build();
  }

  @ServerExceptionMapper
  public RestResponse<TokenRequestErrorDTO> mapUnsupportedGrantTypeException(
      UnsupportedGrantTypeException unsupportedGrantTypeException) {
    String message = "The given authorization grant type is not supported.";
    return RestResponse.status(BAD_REQUEST,
        buildTokenRequestErrorDTO(
            unsupportedGrantTypeException.getMessage(), message));
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapInvalidClientException(
      InvalidClientException invalidClientException) {
    return
        ResponseBuilder
            .create(RestResponse.Status.UNAUTHORIZED)
            .header("WWW-Authenticate", "Basic")
            .entity(buildTokenRequestErrorDTO(
                invalidClientException.getMessage(), invalidClientException.getErrorMessage()))
            .type(MediaType.APPLICATION_JSON_TYPE).build();
  }

  @ServerExceptionMapper
  public RestResponse<TokenRequestErrorDTO> mapInvalidRequestMalformedHeaderException(
      InvalidRequestMalformedHeaderAuthorizationException invalidRequestMalformedHeaderException) {
    String message = "The authorization header is malformed.";
    return RestResponse.status(BAD_REQUEST,
        buildTokenRequestErrorDTO(
            invalidRequestMalformedHeaderException.getMessage(), message));
  }

  @ServerExceptionMapper
  public RestResponse<TokenRequestErrorDTO> mapInvalidGrantException(
      InvalidGrantException invalidGrantException) {
    String message = "The provided authorization grant is invalid.";
    return RestResponse.status(BAD_REQUEST,
        buildTokenRequestErrorDTO(
            invalidGrantException.getMessage(), message));
  }

  private TokenRequestErrorDTO buildTokenRequestErrorDTO(
      String error, String errorDescription) {
    return TokenRequestErrorDTO.builder()
        .error(error)
        .errorDescription(errorDescription)
        .build();
  }

  private RestResponse<Object> genericHTMLError(String errorCode) {
    try {
      return ResponseBuilder
          .create(FOUND)
          .location(new URI(
              SERVICE_PROVIDER_URI + "/login/error?errorCode=" + URLEncoder.encode(errorCode,
                  StandardCharsets.UTF_8))).build();
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
      Log.error("invalid URI for redirecting: "
          + e.getMessage());
      return genericHTMLError(ErrorCode.AUTHORIZATION_ERROR.getErrorCode());
    }
  }

  private ErrorResponse buildErrorResponse(Response.Status status, String message) {
    return ErrorResponse.builder()
        .title(status.getReasonPhrase())
        .status(status.getStatusCode())
        .detail(message)
        .build();
  }

  private RestResponse<Object> buildViolationReportResponse(ConstraintViolationException cve) {
    Status status = Status.BAD_REQUEST;

    List<Violation> violationsInReport = new ArrayList<>(cve.getConstraintViolations().size());
    for (ConstraintViolation<?> cv : cve.getConstraintViolations()) {
      violationsInReport.add(
          new ViolationReport.Violation(cv.getPropertyPath().toString(), cv.getMessage()));
    }

    return ResponseBuilder
        .create(status)
        .header(VALIDATION_HEADER, "true")
        .entity(new ViolationReport("Constraint Violation", status, violationsInReport))
        .type(MediaType.APPLICATION_JSON_TYPE)
        .build();

  }

  private boolean hasReturnValueViolation(Set<ConstraintViolation<?>> violations) {
    if (violations != null) {
      for (ConstraintViolation<?> violation : violations) {
        if (isReturnValueViolation(violation)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isReturnValueViolation(ConstraintViolation<?> violation) {
    Iterator<Node> nodes = violation.getPropertyPath().iterator();
    Path.Node firstNode = nodes.next();

    if (firstNode.getKind() != ElementKind.METHOD) {
      return false;
    }

    Path.Node secondNode = nodes.next();
    return secondNode.getKind() == ElementKind.RETURN_VALUE;
  }
}
