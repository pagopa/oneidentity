package it.pagopa.oneid.exception.mapper;


import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.FOUND;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import it.pagopa.oneid.exception.CallbackURINotFoundException;
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.IDPNotFoundException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.exception.SAMLUtilsException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMapper {

  // TODO re-add this method
 /* @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapGenericException(Exception genericException) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during execution.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }*/
  
  // TODO refactor this method??
  @ServerExceptionMapper
  public Response mapSAMLResponseStatusException(
      SAMLValidationException samlValidationException) {
    try {
      return Response.status(FOUND).location(new URI("/static/sample_error.html")).build();
    } catch (URISyntaxException e) {
      return Response.status(INTERNAL_SERVER_ERROR).build();
    }

  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapGenericAuthnRequestCreationException(
      GenericAuthnRequestCreationException genericAuthnRequestCreationException) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during generation of AuthnRequest.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapSamlUtilsException(SAMLUtilsException samlUtilsException) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during SAMLUtils execution.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapSessionException(SessionException sessionException) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during Session management.";
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
  public RestResponse<ErrorResponse> mapSAMLValidationException(
      SAMLValidationException samlValidationException) {
    Response.Status status = INTERNAL_SERVER_ERROR;
    String message = "Error during SAML Response validation.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapCallbackUriNotFoundException(
      CallbackURINotFoundException callbackURINotFoundException) {
    Response.Status status = BAD_REQUEST;
    String message = "Callback URI not found.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  // TODO consider adding a new exception for empty client table
  public RestResponse<ErrorResponse> mapClientNotFoundException(
      ClientNotFoundException clientNotFoundException) {
    Response.Status status = BAD_REQUEST;
    String message = "Client not found.";
    return RestResponse.status(status, buildErrorResponse(status, message));
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapIdpNotFoundException(
      IDPNotFoundException idpNotFoundException) {
    Response.Status status = BAD_REQUEST;
    String message = "IDP not found";
    return RestResponse.
        status(status, buildErrorResponse(status, message));
  }

  private ErrorResponse buildErrorResponse(Response.Status status, String message) {
    return ErrorResponse.builder()
        .type(MediaType.APPLICATION_JSON)
        .title(status.getReasonPhrase())
        .status(status.getStatusCode())
        .detail(message)
        .build();
  }
}
