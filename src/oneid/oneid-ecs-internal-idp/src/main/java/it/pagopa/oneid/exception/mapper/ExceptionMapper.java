package it.pagopa.oneid.exception.mapper;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.IDPSessionException;
import it.pagopa.oneid.exception.MalformedAuthnRequestException;
import it.pagopa.oneid.exception.SAMLValidationException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMapper {

  @Inject
  Template error;

  @ServerExceptionMapper
  public Response mapException(Exception exception) {
    Log.error(
        "mapException: " + ExceptionUtils.getStackTrace(exception));
    return buildErrorResponsePage("Si è verificato un errore inaspettato.");
  }

  @ServerExceptionMapper
  public Response mapClientNotFoundException(
      ClientNotFoundException clientNotFoundException) {
    Log.error(
        "mapClientNotFoundException: " + ExceptionUtils.getStackTrace(clientNotFoundException));
    return buildErrorResponsePage("Il client specificato non è stato trovato.");
  }

  @ServerExceptionMapper
  public Response mapMalformedAuthnRequestException(
      MalformedAuthnRequestException malformedAuthnRequestException) {
    Log.error(
        "mapMalformedAuthnRequestException: " + ExceptionUtils.getStackTrace(
            malformedAuthnRequestException));
    return buildErrorResponsePage("La richiesta di autenticazione non è valida.");
  }

  @ServerExceptionMapper
  public Response mapSAMLValidationException(
      SAMLValidationException samlValidationException) {
    Log.error(
        "mapSAMLValidationException: " + ExceptionUtils.getStackTrace(samlValidationException));
    return buildErrorResponsePage("Errore nella validazione SAML.");
  }

  @ServerExceptionMapper
  public Response mapIDPSessionException(
      IDPSessionException idpSessionException) {
    Log.error(
        "mapIDPSessionException: " + ExceptionUtils.getStackTrace(idpSessionException));
    return buildErrorResponsePage("La sessione non è valida o è scaduta.");
  }

  private Response buildErrorResponsePage(String errorMessage) {
    return Response.status(Status.OK)
        .entity(error.data("errorMessage", errorMessage))
        .type(MediaType.TEXT_HTML)
        .build();
  }

}
