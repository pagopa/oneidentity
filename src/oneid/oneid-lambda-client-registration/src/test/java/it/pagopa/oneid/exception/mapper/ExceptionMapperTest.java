package it.pagopa.oneid.exception.mapper;

import static it.pagopa.oneid.model.enums.ClientRegistrationErrorCode.INVALID_CLIENT_REGISTRATION;
import static jakarta.ws.rs.core.Response.Status.BAD_GATEWAY;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.exception.NoMasterKeyException;
import it.pagopa.oneid.common.connector.exception.PDVException;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.model.exception.ClientUtilsException;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.InvalidPDVPlanException;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.exception.SSMUpsertPDVException;
import it.pagopa.oneid.exception.UserIdMismatchException;
import it.pagopa.oneid.model.ErrorResponse;
import it.pagopa.oneid.web.dto.ClientRegistrationErrorDTO;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

@QuarkusTest
class ExceptionMapperTest {

  @Inject
  ExceptionMapper exceptionMapper;

  @Test
  void mapException() {
    // given
    Exception exceptionMock = mock(Exception.class);
    String message = "Error during execution.";
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapException(exceptionMock);
    // then
    assertEquals(message, restResponse.getEntity().getDetail());
    assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), restResponse.getEntity().getStatus());

  }

  @Test
  void mapClientUtilsException() {
    // given
    String message = "Error during ClientUtils execution";
    ClientUtilsException exceptionMock = mock(ClientUtilsException.class);
    when(exceptionMock.getMessage()).thenReturn(message);
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapClientUtilsException(
        exceptionMock);
    // then
    assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), restResponse.getStatus());
    assertEquals(message, restResponse.getEntity().getDetail());
  }

  @Test
  void mapClientRegistrationServiceException() {
    // given
    String message = "Error during Service execution";
    ClientRegistrationServiceException exceptionMock = mock(
        ClientRegistrationServiceException.class);
    when(exceptionMock.getMessage()).thenReturn(message);
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapClientRegistrationServiceException(
        exceptionMock);
    // then
    assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), restResponse.getStatus());
    assertEquals(message, restResponse.getEntity().getDetail());
  }

  @Test
  void mapClientNotFoundException() {
    // given
    ClientNotFoundException exceptionMock = mock(
        ClientNotFoundException.class);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapClientNotFoundException(
        exceptionMock);
    // then
    assertEquals(NOT_FOUND.getStatusCode(), restResponse.getStatus());
  }

  @Test
  void mapAuthorizationErrorException() {
    // given
    AuthorizationErrorException exceptionMock = mock(
        AuthorizationErrorException.class);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapAuthorizationErrorException(
        exceptionMock);
    // then
    assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), restResponse.getStatus());
  }

  @Test
  void mapValidationException_OK() {
    // given
    ResteasyReactiveViolationException exceptionMock = mock(
        ResteasyReactiveViolationException.class);
    // when
    RestResponse<ClientRegistrationErrorDTO> restResponse = exceptionMapper.mapValidationException(
        exceptionMock);
    // then
    assertEquals(BAD_REQUEST.getStatusCode(), restResponse.getStatus());
    assertEquals(INVALID_CLIENT_REGISTRATION.getErrorMessage(),
        restResponse.getEntity().getErrorDescription());
  }

  @Test
  void mapValidationException_invalidInstance() {
    ValidationException exceptionMock = mock(
        ValidationException.class);

    Executable executable = () -> exceptionMapper.mapValidationException(
        exceptionMock);

    assertThrows(ValidationException.class, executable);
  }

  @Test
  void mapBadRequestException() {
    // given
    BadRequestException exceptionMock = mock(
        BadRequestException.class);
    // when
    RestResponse<ClientRegistrationErrorDTO> restResponse = exceptionMapper.mapBadRequestException(
        exceptionMock);
    // then
    assertEquals(BAD_REQUEST.getStatusCode(), restResponse.getStatus());
    assertEquals(INVALID_CLIENT_REGISTRATION.getErrorMessage(),
        restResponse.getEntity().getErrorDescription());
  }

  @Test
  void mapInvalidURIException() {
    // given
    String message = "Invalid URI";
    InvalidUriException exceptionMock = new InvalidUriException(
        message);
    // when
    RestResponse<ClientRegistrationErrorDTO> restResponse = exceptionMapper.mapInvalidURIException(
        exceptionMock);
    // then
    assertEquals(BAD_REQUEST.getStatusCode(), restResponse.getStatus());
    assertEquals(message, restResponse.getEntity().getErrorDescription());
  }

  @Test
  void mapInvalidPDVPlanException() {
    // given
    String message = "Invalid PDV Plan";
    InvalidPDVPlanException exceptionMock = new InvalidPDVPlanException(
        message);
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapInvalidPDVPlanException(
        exceptionMock);
    // then
    assertEquals(BAD_REQUEST.getStatusCode(), restResponse.getStatus());
    assertEquals(message, restResponse.getEntity().getDetail());
  }

  @Test
  void mapSSMPDVExceptionException() {
    // given
    String message = "SSM PDV Error";
    SSMUpsertPDVException exceptionMock = new SSMUpsertPDVException(
        message, "path");
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapSSMPDVException(
        exceptionMock);
    // then
    String expectedMessage = "SSM PDV Error [path=path]";
    assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), restResponse.getStatus());
    assertEquals(expectedMessage, restResponse.getEntity().getDetail());
  }

  @Test
  void mapUserIdMismatchException() {
    // given
    String message = "UserId unauthorized";
    UserIdMismatchException exceptionMock = mock(
        UserIdMismatchException.class);
    when(exceptionMock.getMessage()).thenReturn(message);
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapUserIdMismatchException(
        exceptionMock);
    // then
    assertEquals(FORBIDDEN.getStatusCode(), restResponse.getStatus());
    assertEquals(message, restResponse.getEntity().getDetail());
  }

  @Test
  void mapWebApplicationException() {
    // given
    String message = "Malformed JSON request";
    WebApplicationException exceptionMock = mock(WebApplicationException.class);
    when(exceptionMock.getMessage()).thenReturn(message);
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapWebApplicationException(
        exceptionMock);
    // then
    assertEquals(BAD_REQUEST.getStatusCode(), restResponse.getStatus());
    assertEquals(message, restResponse.getEntity().getDetail());
  }

  @Test
  void mapPdvNoKeyExceptionException() {
    // given
    String message = "No key exception";
    NoMasterKeyException exceptionMock = mock(NoMasterKeyException.class);
    when(exceptionMock.getMessage()).thenReturn(message);
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapPdvNoKeyException(
        exceptionMock);
    // then
    assertEquals(BAD_GATEWAY.getStatusCode(), restResponse.getStatus());
    assertEquals(message, restResponse.getEntity().getDetail());
  }

  @Test
  void mapPdvException_fromCauseResponse_usesCauseHttpStatus() {
    String message = "PDV response not ok: ";
    String payload = "pdv failed";

    Response causeResponse = Response.status(BAD_GATEWAY).entity(payload).build();
    WebApplicationException cause = new WebApplicationException("pdv error", causeResponse);

    PDVException ex = new PDVException(message, BAD_GATEWAY.getStatusCode(), Optional.of(payload), cause);

    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapPdvException(ex);

    assertEquals(BAD_GATEWAY.getStatusCode(), restResponse.getStatus());
    assertEquals(payload, restResponse.getEntity().getDetail());
  }
  

  @Test
  void mapPdvException_statusOnly_withMockito() {
    String message = "PDV with only status";

    PDVException ex = mock(PDVException.class);
    when(ex.getResponse()).thenReturn(null);
    when(ex.getStatus()).thenReturn(BAD_REQUEST.getStatusCode());
    when(ex.getMessage()).thenReturn(message);

    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapPdvException(ex);

    assertEquals(BAD_REQUEST.getStatusCode(), restResponse.getStatus());
    assertEquals(message, restResponse.getEntity().getDetail());
  }

  @Test
  void mapPdvException_noResponse_noStatus_500() {
    // given
    String message = "unknown";
    PDVException ex = new PDVException(message, null, Optional.empty(), null);

    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapPdvException(ex);

    // then
    assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), restResponse.getStatus());
    assertEquals(message, restResponse.getEntity().getDetail());
  }
}