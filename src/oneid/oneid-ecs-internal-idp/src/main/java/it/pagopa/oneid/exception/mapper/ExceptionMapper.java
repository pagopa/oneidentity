package it.pagopa.oneid.exception.mapper;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import io.quarkus.logging.Log;
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.IDPSessionException;
import it.pagopa.oneid.exception.MalformedAuthnRequestException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
    Log.error(
        "mapClientNotFoundException: " + ExceptionUtils.getStackTrace(clientNotFoundException));
    return ResponseBuilder.create(BAD_REQUEST).build();
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapMalformedAuthnRequestException(
      MalformedAuthnRequestException malformedAuthnRequestException) {
    Log.error(
        "mapMalformedAuthnRequestException: " + ExceptionUtils.getStackTrace(
            malformedAuthnRequestException));
    return ResponseBuilder.create(BAD_REQUEST).build();
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapSAMLValidationException(
      SAMLValidationException samlValidationException) {
    Log.error(
        "mapSAMLValidationException: " + ExceptionUtils.getStackTrace(samlValidationException));
    return ResponseBuilder.create(BAD_REQUEST).build();
  }

  @ServerExceptionMapper
  public RestResponse<Object> mapIDPSessionException(
      IDPSessionException idpSessionException) {
    Log.error(
        "mapIDPSessionException: " + ExceptionUtils.getStackTrace(idpSessionException));
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
