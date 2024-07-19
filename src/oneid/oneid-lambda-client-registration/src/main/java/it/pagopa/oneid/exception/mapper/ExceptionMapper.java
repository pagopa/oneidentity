package it.pagopa.oneid.exception.mapper;


import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import io.quarkus.logging.Log;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.ClientUtilsException;
import it.pagopa.oneid.model.ErrorResponse;
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
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during ClientUtils execution";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapClientRegistrationServiceException(
      ClientRegistrationServiceException clientRegistrationServiceException) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during Service execution";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }


  private ErrorResponse buildErrorResponse(Response.Status status, String message) {
    return ErrorResponse.builder()
        .title(status.getReasonPhrase())
        .status(status.getStatusCode())
        .detail(message)
        .build();
  }
}
