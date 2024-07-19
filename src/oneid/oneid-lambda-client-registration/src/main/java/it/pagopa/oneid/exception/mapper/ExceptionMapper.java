package it.pagopa.oneid.exception.mapper;


import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.quarkus.logging.Log;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.ClientUtilsException;
import it.pagopa.oneid.exception.InvalidRedirectURIException;
import it.pagopa.oneid.model.ErrorResponse;
import it.pagopa.oneid.model.enums.ClientRegistrationErrorCode;
import it.pagopa.oneid.web.dto.ClientRegistrationErrorDTO;
import jakarta.validation.ValidationException;
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
  public RestResponse<ClientRegistrationErrorDTO> mapValidationException(
      ValidationException validationException) {
    if (!(validationException instanceof ResteasyReactiveViolationException resteasyViolationException)) {
      // Not a violation in a REST endpoint call, but rather in an internal component.
      // This is an internal error: handle through the QuarkusErrorHandler,
      // which will return HTTP status 500 and log the exception.
      Log.error(validationException.getMessage());
      throw validationException;
    }
    String message = ClientRegistrationErrorCode.INVALID_CLIENT_METADATA.getErrorMessage();
    return RestResponse.status(BAD_REQUEST,
        buildClientRegistrationErrorDTO(ClientRegistrationErrorCode.INVALID_CLIENT_METADATA,
            message));
  }

  @ServerExceptionMapper
  public RestResponse<ClientRegistrationErrorDTO> mapInvalidRedirectURIException(
      InvalidRedirectURIException invalidRedirectURIException) {
    Log.error(ExceptionUtils.getStackTrace(invalidRedirectURIException));
    String message = invalidRedirectURIException.getClientRegistrationErrorCode().getErrorMessage();
    return RestResponse.status(BAD_REQUEST,
        buildClientRegistrationErrorDTO(
            invalidRedirectURIException.getClientRegistrationErrorCode(), message));
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
