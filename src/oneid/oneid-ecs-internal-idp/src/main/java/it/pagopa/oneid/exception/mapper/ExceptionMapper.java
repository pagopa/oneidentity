package it.pagopa.oneid.exception.mapper;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMapper {

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapException(Exception exception) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during execution.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapClientNotFoundException(
      ClientNotFoundException clientNotFoundException) {
    return ResponseBuilder.create(BAD_REQUEST).build();
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapMalformedAuthnRequestException(
      ClientNotFoundException clientNotFoundException) {
    return ResponseBuilder.create(BAD_REQUEST).build();
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapSAMLValidationException(
      SAMLValidationException samlValidationException) {
    return ResponseBuilder.create(BAD_REQUEST).build();
  }

  private ErrorResponse buildErrorResponse(Response.Status status, String message) {
    return it.pagopa.oneid.model.ErrorResponse.builder()
        .title(status.getReasonPhrase())
        .status(status.getStatusCode())
        .detail(message)
        .build();
  }

}
