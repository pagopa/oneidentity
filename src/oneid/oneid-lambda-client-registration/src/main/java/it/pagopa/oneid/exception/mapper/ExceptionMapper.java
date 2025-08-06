package it.pagopa.oneid.exception.mapper;


import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static jakarta.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.model.exception.ClientUtilsException;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.InvalidInputSetException;
import it.pagopa.oneid.exception.InvalidLocalizedContentMapException;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.exception.RefreshSecretException;
import it.pagopa.oneid.exception.UserIdMismatchException;
import it.pagopa.oneid.model.ErrorResponse;
import it.pagopa.oneid.model.enums.ClientRegistrationErrorCode;
import it.pagopa.oneid.web.dto.ClientRegistrationErrorDTO;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMapper {

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapException(Exception exception) {
    Log.error(ExceptionUtils.getStackTrace(
        exception));
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during execution.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapNotSupportedException(
      NotSupportedException notSupportedException) {
    Log.error(ExceptionUtils.getStackTrace(notSupportedException));
    Response.Status status = UNSUPPORTED_MEDIA_TYPE;
    String message = "Not valid content-type header. Expected application/json.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapClientUtilsException(
      ClientUtilsException clientUtilsException) {
    Log.error(ExceptionUtils.getStackTrace(clientUtilsException));
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during ClientUtils execution";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapClientRegistrationServiceException(
      ClientRegistrationServiceException clientRegistrationServiceException) {
    Log.error(ExceptionUtils.getStackTrace(clientRegistrationServiceException));
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during Service execution";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapClientNotFoundException(
      ClientNotFoundException clientNotFoundException) {
    Response.Status status = UNAUTHORIZED;
    String message = "Client not found";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapAuthorizationErrorException(
      AuthorizationErrorException authorizationErrorException) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during Service execution";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ClientRegistrationErrorDTO> mapValidationException(
      ValidationException validationException) {
    if (!(validationException instanceof ResteasyReactiveViolationException resteasyViolationException)) {
      // Not a violation in a REST endpoint call, but rather in an internal component.
      // This is an internal error: handle through the QuarkusErrorHandler,
      // which will return HTTP status 500 and log the exception.
      Log.error(validationException.getMessage());
      throw validationException;
    }
    String message = ClientRegistrationErrorCode.INVALID_CLIENT_REGISTRATION.getErrorMessage();
    return RestResponse.status(BAD_REQUEST,
        buildClientRegistrationErrorDTO(ClientRegistrationErrorCode.INVALID_CLIENT_REGISTRATION,
            message));
  }

  @ServerExceptionMapper
  public RestResponse<ClientRegistrationErrorDTO> mapBadRequestException(
      BadRequestException badRequestException) {
    Log.error(ExceptionUtils.getStackTrace(badRequestException));
    return RestResponse.status(BAD_REQUEST,
        buildClientRegistrationErrorDTO(
            ClientRegistrationErrorCode.INVALID_CLIENT_REGISTRATION,
            ClientRegistrationErrorCode.INVALID_CLIENT_REGISTRATION.getErrorMessage()));
  }

  @ServerExceptionMapper
  public RestResponse<ClientRegistrationErrorDTO> mapInvalidURIException(
      InvalidUriException invalidUriException) {
    Log.error(ExceptionUtils.getStackTrace(invalidUriException));
    return RestResponse.status(BAD_REQUEST,
        buildClientRegistrationErrorDTO(
            invalidUriException.getClientRegistrationErrorCode(),
            invalidUriException.getMessage()));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapUserIdMismatchException(
      UserIdMismatchException userIdMismatchException) {
    Log.error(ExceptionUtils.getStackTrace(userIdMismatchException));
    Response.Status status = BAD_REQUEST;
    String message = "Error during Service execution";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapInvalidInputSetException(
      InvalidInputSetException invalidInputSetException) {
    Log.error(ExceptionUtils.getStackTrace(invalidInputSetException));
    Response.Status status = BAD_REQUEST;
    return RestResponse.status(status,
        buildErrorResponse(status, invalidInputSetException.getMessage()));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapInvalidLocalizedContentMapException(
      InvalidLocalizedContentMapException invalidLocalizedContentMapException) {
    Log.error(ExceptionUtils.getStackTrace(invalidLocalizedContentMapException));
    Response.Status status = BAD_REQUEST;
    return RestResponse.status(status,
        buildErrorResponse(status, invalidLocalizedContentMapException.getMessage()));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapWebApplicationException(WebApplicationException exception) {
    Log.error(ExceptionUtils.getStackTrace(exception));
    Response.Status status = Response.Status.BAD_REQUEST;
    return RestResponse.status(status, buildErrorResponse(status, exception.getMessage()));
  }


  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapRefreshSecretException(
      RefreshSecretException refreshSecretException) {
    Log.error(ExceptionUtils.getStackTrace(refreshSecretException));
    return RestResponse.status(BAD_REQUEST,
        buildErrorResponse(BAD_REQUEST, refreshSecretException.getMessage()));
  }

  private ErrorResponse buildErrorResponse(Response.Status status, String message) {
    return ErrorResponse.builder()
        .title(status.getReasonPhrase())
        .status(status.getStatusCode())
        .detail(message)
        .build();
  }

  private ClientRegistrationErrorDTO buildClientRegistrationErrorDTO(
      ClientRegistrationErrorCode errorCode, String errorDescription) {
    return ClientRegistrationErrorDTO.builder()
        .error(errorCode.getErrorCode())
        .errorDescription(errorDescription)
        .build();
  }
}
