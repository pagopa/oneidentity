package it.pagopa.oneid.exception.mapper;

import static it.pagopa.oneid.model.enums.ClientRegistrationErrorCode.INVALID_CLIENT_METADATA;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.model.exception.ClientUtilsException;
import it.pagopa.oneid.exception.ClientRegistrationServiceException;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.model.ErrorResponse;
import it.pagopa.oneid.web.dto.ClientRegistrationErrorDTO;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.BadRequestException;
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
    assertEquals(UNAUTHORIZED.getStatusCode(), restResponse.getStatus());
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
    assertEquals(INVALID_CLIENT_METADATA.getErrorMessage(),
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
    assertEquals(INVALID_CLIENT_METADATA.getErrorMessage(),
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
}