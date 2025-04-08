package it.pagopa.oneid.exception.mapper;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.FOUND;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.common.model.exception.ClientUtilsException;
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
import jakarta.inject.Inject;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.Path.Node;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class ExceptionMapperTest {

  private final String DETAIL_MESSAGE = "detail_message";

  private final String DEFAULT_FALLBACK_URI = "test.com";

  private final String DEFAULT_STATE = "dummyState";

  private final String DEFAULT_CLIENT_ID = "dummyClientId";

  @Inject
  ExceptionMapper exceptionMapper;

  @Test
  void mapException() {
    // given
    Exception exceptionMock = Mockito.mock(Exception.class);
    String message = "Error during execution.";
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapException(exceptionMock);
    // then
    checkErrorWithBuildErrorResponse(INTERNAL_SERVER_ERROR, message, restResponse);
  }

  @Test
  void mapJakartaResourceNotFoundException() {
    String message = "Error during execution.";
    // given
    jakarta.ws.rs.NotFoundException exceptionMock = Mockito.mock(
        jakarta.ws.rs.NotFoundException.class);
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapJakartaResourceNotFoundException(
        exceptionMock);
    // then
    assertNotNull(restResponse);
    assertNotNull(restResponse.getEntity());
    assertEquals(message, restResponse.getEntity().getDetail());
    assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), restResponse.getEntity().getStatus());
  }

  @Test
  void mapGenericHTMLException() {
    // given
    GenericHTMLException exceptionMock = Mockito.mock(GenericHTMLException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn(DETAIL_MESSAGE);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapGenericHTMLException(
        exceptionMock);
    // then
    checkErrorWithGenericHTMLError(FOUND, exceptionMock.getMessage(), restResponse);
  }

  // Only this method, between the ones that use genericHTMLError, will trigger the URISyntaxException
  @Test
  void mapGenericHTMLException_InternalServerError() {
    // given
    GenericHTMLException exceptionMock = Mockito.mock(GenericHTMLException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn(null);

    // when
    RestResponse<Object> restResponse = exceptionMapper.mapGenericHTMLException(
        exceptionMock);
    // then

    checkErrorWithGenericHTMLError(INTERNAL_SERVER_ERROR, "", restResponse);
  }

  @Test
  void mapValidationException_AuthorizationErrorException() {
    // given
    ValidationException exceptionMock = Mockito.mock(ValidationException.class);
    AuthorizationErrorException authorizationErrorExceptionMock = Mockito.mock(
        AuthorizationErrorException.class);
    Mockito.when(authorizationErrorExceptionMock.getErrorMessage()).thenReturn("test");
    Mockito.when(authorizationErrorExceptionMock.getOAuth2errorCode())
        .thenReturn(OAuth2Error.INVALID_REQUEST_CODE);
    Mockito.when(exceptionMock.getCause()).thenReturn(authorizationErrorExceptionMock);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapValidationException(
        exceptionMock);
    // then
    checkErrorWithAuthenticationErrorResponse(FOUND, OAuth2Error.INVALID_REQUEST_CODE,
        restResponse);
    assertFalse(restResponse.getLocation().toString().contains("&state=test"));
  }

  @Test
  void mapValidationException_GenericHTMLException() {
    // given
    ValidationException exceptionMock = Mockito.mock(ValidationException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn(DETAIL_MESSAGE);
    GenericHTMLException genericHTMLException = Mockito.mock(
        GenericHTMLException.class);
    Mockito.when(genericHTMLException.getMessage()).thenReturn(DETAIL_MESSAGE);
    Mockito.when(exceptionMock.getCause()).thenReturn(genericHTMLException);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapValidationException(
        exceptionMock);
    // then
    checkErrorWithGenericHTMLError(FOUND, exceptionMock.getMessage(), restResponse);
  }

  @Test
  void mapValidationException_MessageAuthorizeGet() {
    // given
    ValidationException exceptionMock = Mockito.mock(ValidationException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn("authorizeGet.arg0.clientId");
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapValidationException(
        exceptionMock);
    // then
    checkErrorWithGenericHTMLError(FOUND, ErrorCode.GENERIC_HTML_ERROR.getErrorMessage(),
        restResponse);
  }

  @Test
  void mapValidationException_MessageAuthorizePost() {
    // given
    ValidationException exceptionMock = Mockito.mock(ValidationException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn("authorizePost.arg0.clientId");
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapValidationException(
        exceptionMock);
    // then
    checkErrorWithGenericHTMLError(FOUND, ErrorCode.GENERIC_HTML_ERROR.getErrorMessage(),
        restResponse);
  }

  @Test
  void mapValidationException_MessageToken() {
    // given
    ValidationException exceptionMock = Mockito.mock(ValidationException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn("token.arg0.test");
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapValidationException(
        exceptionMock);
    // then
    assertNotNull(restResponse);
    assertEquals(BAD_REQUEST.getStatusCode(), restResponse.getStatus());
    assertNotNull(restResponse.getEntity());
  }

  @Test
  void mapValidationException_NotResteasyReactiveViolationException() {
    // given
    ValidationException exceptionMock = Mockito.mock(ValidationException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn(DETAIL_MESSAGE);

    // then
    assertThrows(ValidationException.class, () -> exceptionMapper.mapValidationException(
        exceptionMock));
  }

  @Test
  void mapValidationException_HasReturnValueViolationTrue() {
    // given
    ResteasyReactiveViolationException exceptionMock = Mockito.mock(
        ResteasyReactiveViolationException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn(DETAIL_MESSAGE);
    ConstraintViolationImpl<?> violationMock = Mockito.mock(ConstraintViolationImpl.class);
    Mockito.when(violationMock.getMessage()).thenReturn("test");

    //Path
    Path pathMock = Mockito.mock(Path.class);
    Mockito.when(pathMock.toString()).thenReturn("test");

    //Path.Node
    Path.Node nodeMock_1 = Mockito.mock(Path.Node.class);
    Path.Node nodeMock_2 = Mockito.mock(Path.Node.class);

    Mockito.when(nodeMock_1.getKind()).thenReturn(ElementKind.METHOD);
    Mockito.when(nodeMock_2.getKind()).thenReturn(ElementKind.RETURN_VALUE);

    List<Node> nodeSet = List.of(nodeMock_1, nodeMock_2);
    Iterator<Path.Node> iteratorNode = nodeSet.iterator();

    Mockito.when(pathMock.iterator()).thenReturn(iteratorNode);

    Mockito.when(violationMock.getPropertyPath()).thenReturn(pathMock);
    Mockito.when(exceptionMock.getConstraintViolations()).thenReturn(Set.of(violationMock));

    // then
    assertThrows(ResteasyReactiveViolationException.class,
        () -> exceptionMapper.mapValidationException(
            exceptionMock));

  }

  @Test
  void mapValidationException_HasReturnValueViolationNull() {
    // given
    ResteasyReactiveViolationException exceptionMock = Mockito.mock(
        ResteasyReactiveViolationException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn(DETAIL_MESSAGE);
    ConstraintViolationImpl<?> violationMock = Mockito.mock(ConstraintViolationImpl.class);
    Mockito.when(violationMock.getMessage()).thenReturn("test");

    Mockito.when(exceptionMock.getConstraintViolations()).thenReturn(null);

    // when
    RestResponse<Object> restResponse = exceptionMapper.mapValidationException(
        exceptionMock);

    // then
    assertNotNull(restResponse);
    assertNotNull(restResponse.getEntity());
  }

  @Test
  void mapValidationException_IsReturnValueViolationFirstIf() {
    // given
    ResteasyReactiveViolationException exceptionMock = Mockito.mock(
        ResteasyReactiveViolationException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn(DETAIL_MESSAGE);
    ConstraintViolationImpl<?> violationMock = Mockito.mock(ConstraintViolationImpl.class);
    Mockito.when(violationMock.getMessage()).thenReturn("test");

    //Path
    Path pathMock = Mockito.mock(Path.class);
    Mockito.when(pathMock.toString()).thenReturn("test");

    //Path.Node
    Path.Node nodeMock_1 = Mockito.mock(Path.Node.class);

    Mockito.when(nodeMock_1.getKind()).thenReturn(ElementKind.RETURN_VALUE);

    List<Node> nodeSet = List.of(nodeMock_1);
    Iterator<Path.Node> iteratorNode = nodeSet.iterator();

    Mockito.when(pathMock.iterator()).thenReturn(iteratorNode);

    Mockito.when(violationMock.getPropertyPath()).thenReturn(pathMock);
    Mockito.when(exceptionMock.getConstraintViolations()).thenReturn(Set.of(violationMock));

    // when
    RestResponse<Object> restResponse = exceptionMapper.mapValidationException(
        exceptionMock);
    // then
    assertNotNull(restResponse);
    assertEquals(BAD_REQUEST.getStatusCode(), restResponse.getStatus());
    assertNotNull(restResponse.getHeaders());
    assertTrue(restResponse.getHeaders().containsKey("validation-exception"));
    assertEquals(MediaType.APPLICATION_JSON_TYPE, restResponse.getMediaType());
    assertNotNull(restResponse.getEntity());
  }

  @Test
  void mapValidationException_HasReturnValueViolationFalse() {
    // given
    ResteasyReactiveViolationException exceptionMock = Mockito.mock(
        ResteasyReactiveViolationException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn(DETAIL_MESSAGE);
    ConstraintViolationImpl<?> violationMock = Mockito.mock(ConstraintViolationImpl.class);
    Mockito.when(violationMock.getMessage()).thenReturn("test");

    //Path
    Path pathMock = Mockito.mock(Path.class);
    Mockito.when(pathMock.toString()).thenReturn("test");

    //Path.Node
    Path.Node nodeMock_1 = Mockito.mock(Path.Node.class);
    Path.Node nodeMock_2 = Mockito.mock(Path.Node.class);

    Mockito.when(nodeMock_1.getKind()).thenReturn(ElementKind.METHOD);
    Mockito.when(nodeMock_2.getKind()).thenReturn(ElementKind.METHOD);

    List<Node> nodeSet = List.of(nodeMock_1, nodeMock_2);
    Iterator<Path.Node> iteratorNode = nodeSet.iterator();

    Mockito.when(pathMock.iterator()).thenReturn(iteratorNode);

    Mockito.when(violationMock.getPropertyPath()).thenReturn(pathMock);
    Mockito.when(exceptionMock.getConstraintViolations()).thenReturn(Set.of(violationMock));

    // when
    RestResponse<Object> restResponse = exceptionMapper.mapValidationException(
        exceptionMock);
    // then
    assertNotNull(restResponse);
    assertEquals(BAD_REQUEST.getStatusCode(), restResponse.getStatus());
    assertNotNull(restResponse.getHeaders());
    assertTrue(restResponse.getHeaders().containsKey("validation-exception"));
    assertEquals(MediaType.APPLICATION_JSON_TYPE, restResponse.getMediaType());
    assertNotNull(restResponse.getEntity());
  }

  @Test
  void mapSAMLResponseStatusException() {
    // given
    SAMLResponseStatusException exceptionMock = Mockito.mock(SAMLResponseStatusException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn(DETAIL_MESSAGE);
    Mockito.when(exceptionMock.getRedirectUri()).thenReturn(DEFAULT_FALLBACK_URI);
    Mockito.when(exceptionMock.getState()).thenReturn(DEFAULT_STATE);
    Mockito.when(exceptionMock.getClientId()).thenReturn(DEFAULT_CLIENT_ID);
    RestResponse<Object> restResponse = exceptionMapper.mapSAMLResponseStatusException(
        exceptionMock);
    // then
    checkErrorWithGenericHTMLError(FOUND, exceptionMock.getMessage(), restResponse);
  }

  @Test
  void mapSAMLValidationException() {
    // given
    SAMLValidationException exceptionMock = Mockito.mock(SAMLValidationException.class);
    Mockito.when(exceptionMock.getErrorCode()).thenReturn(ErrorCode.IDP_ERROR_ISSUER_VALUE_BLANK);
    Mockito.when(exceptionMock.getMessage())
        .thenReturn(ErrorCode.IDP_ERROR_ISSUER_VALUE_BLANK.getErrorMessage());
    Mockito.when(exceptionMock.getRedirectUri()).thenReturn("test.com");
    Mockito.when(exceptionMock.getState()).thenReturn("dummyState");
    Mockito.when(exceptionMock.getClientId()).thenReturn("dummyClientId");

    // when
    RestResponse<Object> restResponse = exceptionMapper.mapSAMLValidationException(
        exceptionMock);
    // then
    checkErrorWithGenericHTMLError(FOUND, exceptionMock.getErrorCode().getErrorCode(),
        restResponse);
  }

  @Test
  void mapGenericAuthnRequestCreationException() {
    // given
    GenericAuthnRequestCreationException exceptionMock = Mockito.mock(
        GenericAuthnRequestCreationException.class);
    String message = "Error during generation of AuthnRequest.";
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapGenericAuthnRequestCreationException(
        exceptionMock);
    // then
    checkErrorWithBuildErrorResponse(INTERNAL_SERVER_ERROR, message, restResponse);
  }

  @Test
  void mapOIDCSignJWTException() {
    // given
    OIDCSignJWTException exceptionMock = Mockito.mock(OIDCSignJWTException.class);
    String message = "Error during signing of JWT.";
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapOIDCSignJWTException(
        exceptionMock);
    // then
    checkErrorWithBuildErrorResponse(INTERNAL_SERVER_ERROR, message, restResponse);
  }

  @Test
  void mapIDPSSOEndpointNotFoundException() {
    // given
    IDPSSOEndpointNotFoundException exceptionMock = Mockito.mock(
        IDPSSOEndpointNotFoundException.class);
    String message = "IDPSSO endpoint not found for selected idp.";
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapIDPSSOEndpointNotFoundException(
        exceptionMock);
    // then
    checkErrorWithBuildErrorResponse(INTERNAL_SERVER_ERROR, message, restResponse);
  }

  @Test
  void mapCallbackUriNotFoundException() {
    // given
    CallbackURINotFoundException exceptionMock = Mockito.mock(CallbackURINotFoundException.class);
    Mockito.when(exceptionMock.getMessage()).thenReturn(DETAIL_MESSAGE);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapCallbackUriNotFoundException(
        exceptionMock);
    // then
    checkErrorWithGenericHTMLError(FOUND, exceptionMock.getMessage(), restResponse);
  }

  // This method test also the getUri with the state parameter
  @Test
  void mapClientNotFoundException_WithState() {
    // given
    ClientNotFoundException exceptionMock = Mockito.mock(ClientNotFoundException.class);
    Mockito.when(exceptionMock.getState()).thenReturn("test");
    Mockito.when(exceptionMock.getErrorMessage()).thenReturn("test");
    Mockito.when(exceptionMock.getOAuth2errorCode())
        .thenReturn(OAuth2Error.UNAUTHORIZED_CLIENT_CODE);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapClientNotFoundException(
        exceptionMock);
    // then
    checkErrorWithAuthenticationErrorResponse(FOUND, OAuth2Error.UNAUTHORIZED_CLIENT_CODE,
        restResponse);
    assertTrue(restResponse.getLocation().toString().contains("&state=test"));
  }

  // This method test also the getUri without the state parameter
  @Test
  void mapClientNotFoundException_NoState() {
    // given
    ClientNotFoundException exceptionMock = Mockito.mock(ClientNotFoundException.class);
    Mockito.when(exceptionMock.getErrorMessage()).thenReturn("test");
    Mockito.when(exceptionMock.getOAuth2errorCode())
        .thenReturn(OAuth2Error.UNAUTHORIZED_CLIENT_CODE);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapClientNotFoundException(
        exceptionMock);
    // then
    checkErrorWithAuthenticationErrorResponse(FOUND, OAuth2Error.UNAUTHORIZED_CLIENT_CODE,
        restResponse);
    assertFalse(restResponse.getLocation().toString().contains("&state=test"));
  }

  // Only this method, between the ones that use authenticationErrorResponse, will trigger the URISyntaxException
  @Test
  void mapClientNotFoundExceptionURISyntaxException() {
    // given
    ClientNotFoundException exceptionMock = Mockito.mock(ClientNotFoundException.class);
    Mockito.when(exceptionMock.getErrorMessage()).thenReturn(null);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapClientNotFoundException(
        exceptionMock);
    // then
    checkErrorWithAuthenticationErrorResponse(FOUND, ErrorCode.AUTHORIZATION_ERROR.getErrorCode(),
        restResponse);
  }

  @Test
  void mapInvalidScopeException() {
    // given
    InvalidScopeException exceptionMock = Mockito.mock(InvalidScopeException.class);
    Mockito.when(exceptionMock.getState()).thenReturn("test");
    Mockito.when(exceptionMock.getErrorMessage()).thenReturn("test");
    Mockito.when(exceptionMock.getOAuth2errorCode())
        .thenReturn(OAuth2Error.INVALID_SCOPE_CODE);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapInvalidScopeException(
        exceptionMock);
    // then
    checkErrorWithAuthenticationErrorResponse(FOUND, OAuth2Error.INVALID_SCOPE_CODE,
        restResponse);
    assertTrue(restResponse.getLocation().toString().contains("&state=test"));
  }

  @Test
  void mapUnsupportedResponseTypeException() {
    // given
    UnsupportedResponseTypeException exceptionMock = Mockito.mock(
        UnsupportedResponseTypeException.class);
    Mockito.when(exceptionMock.getState()).thenReturn("test");
    Mockito.when(exceptionMock.getErrorMessage()).thenReturn("test");
    Mockito.when(exceptionMock.getOAuth2errorCode())
        .thenReturn(OAuth2Error.UNSUPPORTED_RESPONSE_TYPE_CODE);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapUnsupportedResponseTypeException(
        exceptionMock);
    // then
    checkErrorWithAuthenticationErrorResponse(FOUND, OAuth2Error.UNSUPPORTED_RESPONSE_TYPE_CODE,
        restResponse);
    assertTrue(restResponse.getLocation().toString().contains("&state=test"));
  }

  @Test
  void mapAuthorizationErrorException() {
    // given
    AuthorizationErrorException exceptionMock = Mockito.mock(AuthorizationErrorException.class);
    Mockito.when(exceptionMock.getState()).thenReturn("test");
    Mockito.when(exceptionMock.getErrorMessage()).thenReturn("test");
    Mockito.when(exceptionMock.getOAuth2errorCode())
        .thenReturn(OAuth2Error.SERVER_ERROR_CODE);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapAuthorizationErrorException(
        exceptionMock);
    // then
    checkErrorWithAuthenticationErrorResponse(FOUND, OAuth2Error.SERVER_ERROR_CODE,
        restResponse);
    assertTrue(restResponse.getLocation().toString().contains("&state=test"));
  }

  @Test
  void mapIDPNotFoundException() {
    // given
    IDPNotFoundException exceptionMock = Mockito.mock(IDPNotFoundException.class);
    Mockito.when(exceptionMock.getState()).thenReturn("test");
    Mockito.when(exceptionMock.getErrorMessage()).thenReturn("test");
    Mockito.when(exceptionMock.getOAuth2errorCode())
        .thenReturn(OAuth2Error.INVALID_REQUEST_CODE);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapIDPNotFoundException(
        exceptionMock);
    // then
    checkErrorWithAuthenticationErrorResponse(FOUND, OAuth2Error.INVALID_REQUEST_CODE,
        restResponse);
    assertTrue(restResponse.getLocation().toString().contains("&state=test"));
  }

  @Test
  void mapOIDCAuthorizationException() {
    // given
    OIDCAuthorizationException exceptionMock = Mockito.mock(OIDCAuthorizationException.class);
    String message = "Error during OIDC authorization flow.";
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapOIDCAuthorizationException(
        exceptionMock);
    // then
    checkErrorWithBuildErrorResponse(BAD_REQUEST, message, restResponse);
  }

  @Test
  void mapAssertionNotFoundException() {
    // given
    AssertionNotFoundException exceptionMock = Mockito.mock(AssertionNotFoundException.class);
    String message = "Assertion not found.";
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapAssertionNotFoundException(
        exceptionMock);
    // then
    checkErrorWithBuildErrorResponse(NOT_FOUND, message, restResponse);
    assertEquals(MediaType.APPLICATION_JSON_TYPE, restResponse.getMediaType());
  }

  @Test
  void mapUnsupportedGrantTypeException() {
    // given
    String message = "The given authorization grant type is not supported.";
    UnsupportedGrantTypeException exceptionMock = Mockito.mock(
        UnsupportedGrantTypeException.class);
    Mockito.when(exceptionMock.getMessage())
        .thenReturn(DETAIL_MESSAGE);
    // when
    RestResponse<TokenRequestErrorDTO> restResponse = exceptionMapper.mapUnsupportedGrantTypeException(
        exceptionMock);
    // then
    checkErrorWithBuildTokenRequestErrorDTO(message, BAD_REQUEST, restResponse);
  }

  @Test
  void mapClientUtilsException() {
    // given
    ClientUtilsException exceptionMock = Mockito.mock(ClientUtilsException.class);
    String message = "Error during ClientUtils execution";
    // when
    RestResponse<ErrorResponse> restResponse = exceptionMapper.mapClientUtilsException(
        exceptionMock);
    // then
    checkErrorWithBuildErrorResponse(INTERNAL_SERVER_ERROR, message, restResponse);
  }

  @Test
  void mapInvalidClientException() {
    // given
    String message = "The authorization header is malformed.";
    InvalidClientException exceptionMock = Mockito.mock(
        InvalidClientException.class);
    Mockito.when(exceptionMock.getMessage())
        .thenReturn(DETAIL_MESSAGE);
    // when
    RestResponse<Object> restResponse = exceptionMapper.mapInvalidClientException(
        exceptionMock);
    // then
    assertNotNull(restResponse);
    assertEquals(UNAUTHORIZED.getStatusCode(), restResponse.getStatus());
    assertNotNull(restResponse.getEntity());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, restResponse.getMediaType());
    assertNotNull(restResponse.getHeaders());
    assertTrue(restResponse.getHeaders().containsKey("WWW-Authenticate"));
    assertTrue(restResponse.getHeaders().get("WWW-Authenticate").contains("Basic"));
  }

  @Test
  void mapInvalidRequestMalformedHeaderException() {
    // given
    String message = "The authorization header is malformed.";
    InvalidRequestMalformedHeaderAuthorizationException exceptionMock = Mockito.mock(
        InvalidRequestMalformedHeaderAuthorizationException.class);
    Mockito.when(exceptionMock.getMessage())
        .thenReturn(DETAIL_MESSAGE);
    // when
    RestResponse<TokenRequestErrorDTO> restResponse = exceptionMapper.mapInvalidRequestMalformedHeaderException(
        exceptionMock);
    // then
    checkErrorWithBuildTokenRequestErrorDTO(message, BAD_REQUEST, restResponse);
  }

  @Test
  void mapInvalidGrantException() {
    // given
    String message = "The provided authorization grant is invalid.";
    InvalidGrantException exceptionMock = Mockito.mock(
        InvalidGrantException.class);
    Mockito.when(exceptionMock.getMessage())
        .thenReturn(DETAIL_MESSAGE);
    // when
    RestResponse<TokenRequestErrorDTO> restResponse = exceptionMapper.mapInvalidGrantException(
        exceptionMock);
    // then
    checkErrorWithBuildTokenRequestErrorDTO(message, BAD_REQUEST, restResponse);
  }


  //region private methods
  private void checkErrorWithBuildTokenRequestErrorDTO(String message, Status status,
      RestResponse<TokenRequestErrorDTO> restResponse) {
    assertNotNull(restResponse);
    assertEquals(status.getStatusCode(), restResponse.getStatus());
    assertNotNull(restResponse.getEntity());
    assertEquals(DETAIL_MESSAGE, restResponse.getEntity().getError());
    assertEquals(message, restResponse.getEntity().getErrorDescription());
  }

  private void checkErrorWithBuildErrorResponse(Status status, String message,
      RestResponse<ErrorResponse> restResponse) {
    assertNotNull(restResponse);
    assertNotNull(restResponse.getEntity());
    assertEquals(message, restResponse.getEntity().getDetail());
    assertEquals(status.getStatusCode(), restResponse.getEntity().getStatus());
  }

  private void checkErrorWithGenericHTMLError(Status status, String errorCode,
      RestResponse<Object> restResponse) {
    assertNotNull(restResponse);
    assertEquals(status.getStatusCode(), restResponse.getStatus());
    if (restResponse.getStatus() != INTERNAL_SERVER_ERROR.getStatusCode()) {
      assertNotNull(restResponse.getLocation());
      assertTrue(restResponse.getLocation().toString().contains(URLEncoder.encode(errorCode,
          StandardCharsets.UTF_8)));
    }
  }

  private void checkErrorWithAuthenticationErrorResponse(Status status, String errorMessage,
      RestResponse<Object> restResponse) {
    assertNotNull(restResponse);
    assertEquals(status.getStatusCode(), restResponse.getStatus());
    if (restResponse.getStatus() != INTERNAL_SERVER_ERROR.getStatusCode()) {
      assertNotNull(restResponse.getLocation());
      System.out.println(restResponse.getLocation().toString());
      assertTrue(restResponse.getLocation().toString().contains(errorMessage));
    }
  }
  //endregion
}