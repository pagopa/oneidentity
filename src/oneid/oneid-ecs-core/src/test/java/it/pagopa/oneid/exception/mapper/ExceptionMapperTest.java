package it.pagopa.oneid.exception.mapper;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.FOUND;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
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
import it.pagopa.oneid.exception.InvalidAccessTokenException;
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
import it.pagopa.oneid.service.SAMLErrorRedirectService;
import it.pagopa.oneid.web.dto.TokenRequestErrorDTO;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.Path.Node;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import java.net.URLEncoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

class ExceptionMapperTest {

        private final String DETAIL_MESSAGE = "detail_message";

        private final String DEFAULT_FALLBACK_URI = "test.com";

        private final String DEFAULT_STATE = "dummyState";

        private final String DEFAULT_CLIENT_ID = "dummyClientId";

        private final String DEFAULT_ERROR_CODE = "dummyErrorCode";

        ExceptionMapper exceptionMapper;

        SAMLErrorRedirectService samlErrorRedirectService;

        @BeforeEach
        void setUpSamlErrorRedirectFallback() {
                exceptionMapper = new ExceptionMapper();
                exceptionMapper.BASE_PATH = "https://oneid.example";
                exceptionMapper.cloudWatchConnectorImpl = Mockito.mock(CloudWatchConnectorImpl.class);
                samlErrorRedirectService = Mockito.mock(SAMLErrorRedirectService.class);
                exceptionMapper.samlErrorRedirectService = samlErrorRedirectService;
                Mockito.when(samlErrorRedirectService.resolveRedirect(Mockito.any()))
                                .thenReturn(Optional.empty());
        }

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

        @Test
        @DisplayName("Cookie rejection returns a 302 error-page redirect with encoded callback context")
        void givenCallbackContext_whenMappingGenericHtmlError_thenRedirectWithContext() {
                String redirectUri = "https://client.example/callback?flow=login&source=web";
                String state = "saved state&value=1";
                String clientId = "client+id";
                GenericHTMLException exception = new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR,
                                redirectUri, state, clientId);

                RestResponse<Object> response = exceptionMapper.mapGenericHTMLException(exception);

                assertEquals(FOUND.getStatusCode(), response.getStatus());
                String query = response.getLocation().getRawQuery();
                assertEquals("error_code=GENERIC_HTML_ERROR&redirect_uri="
                                + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8), query);
                assertTrue(response.getLocation().getPath().endsWith("/login/error"));
                assertNull(response.getEntity());
        }

        @ParameterizedTest
        @CsvSource(value = {
                        "NULL,saved-state,client-id",
                        "https://client.example/callback,saved-state,NULL"
        }, nullValues = "NULL")
        @DisplayName("Incomplete callback context retains the generic 302 redirect rather than returning 500")
        void givenIncompleteCallbackContext_whenMappingGenericHtmlError_thenGenericRedirect(
                        String redirectUri, String state, String clientId) {
                GenericHTMLException exception = new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR,
                                redirectUri, state, clientId);

                RestResponse<Object> response = exceptionMapper.mapGenericHTMLException(exception);

                assertEquals(FOUND.getStatusCode(), response.getStatus());
                assertEquals("error_code=GENERIC_HTML_ERROR", response.getLocation().getRawQuery());
        }

        @Test
        @DisplayName("Absent state retains callback context with the legacy null state value")
        void given_callback_without_state_when_mapping_then_contextual_redirect() {
                RestResponse<Object> response = exceptionMapper.mapGenericHTMLException(
                                new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR,
                                                "https://client.example/callback", null, "client-id"));

                assertEquals(FOUND.getStatusCode(), response.getStatus());
                assertEquals("error_code=GENERIC_HTML_ERROR&redirect_uri="
                                + URLEncoder.encode("https://client.example/callback", StandardCharsets.UTF_8)
                                + "&state=null&client_id=client-id", response.getLocation().getRawQuery());
        }

        @ParameterizedTest
        @CsvSource(value = {
                        "status,https://client.example/callback,client-id",
                        "validation,https://client.example/callback,client-id",
                        "status,NULL,client-id",
                        "validation,NULL,client-id",
                        "status,https://client.example/callback,NULL",
                        "validation,https://client.example/callback,NULL"
        }, nullValues = "NULL")
        @DisplayName("SAML errors with null state redirect instead of returning 500")
        void given_null_state_when_mapping_saml_error_then_redirect(
                        String errorType, String redirectUri, String clientId) {
                RestResponse<Object> response;
                if ("status".equals(errorType)) {
                        response = exceptionMapper.mapSAMLResponseStatusException(
                                        new SAMLResponseStatusException(ErrorCode.ERRORCODE_NR22,
                                                        redirectUri, clientId, "idp", null));
                } else {
                        SAMLValidationException exception = new SAMLValidationException(ErrorCode.ERRORCODE_NR22);
                        exception.setRedirectUri(redirectUri);
                        exception.setClientId(clientId);
                        exception.setIdp("idp");
                        response = exceptionMapper.mapSAMLValidationException(exception);
                }

                assertEquals(FOUND.getStatusCode(), response.getStatus());
                assertEquals("/login/error", response.getLocation().getPath());
                String expectedQuery = "error_code=22";
                if (redirectUri != null && clientId != null) {
                        expectedQuery += "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                                        + "&state=null&client_id=client-id";
                }
                assertEquals(expectedQuery, response.getLocation().getRawQuery());
        }

        @Test
        @DisplayName("Authorization errors preserve callback queries and encode literal OAuth values")
        void given_callback_query_and_reserved_state_when_mapping_then_preserve_values() {
                AuthorizationErrorException exception = Mockito.mock(AuthorizationErrorException.class);
                String state = "saved state&value=1+%{token}";
                String description = "error & details+%{literal}";
                Mockito.when(exception.getCallbackUri()).thenReturn(
                                "https://client.example/callback?path=%2Farea%26x%25&error=old&state=old");
                Mockito.when(exception.getState()).thenReturn(state);
                Mockito.when(exception.getErrorMessage()).thenReturn(description);
                Mockito.when(exception.getOAuth2errorCode()).thenReturn(OAuth2Error.INVALID_REQUEST_CODE);

                RestResponse<Object> response = exceptionMapper.mapAuthorizationErrorException(exception);

                assertEquals(FOUND.getStatusCode(), response.getStatus());
                assertEquals("path=%2Farea%26x%25&error=invalid_request&error_description="
                                + URLEncoder.encode(description, StandardCharsets.UTF_8)
                                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8),
                                response.getLocation().getRawQuery());
        }

        @Test
        @DisplayName("Authorization errors without state retain existing callback state")
        void given_absent_state_when_mapping_then_preserve_callback_state() {
                AuthorizationErrorException exception = Mockito.mock(AuthorizationErrorException.class);
                Mockito.when(exception.getCallbackUri()).thenReturn(
                                "https://client.example/callback?source=oneid&state=old");
                Mockito.when(exception.getErrorMessage()).thenReturn("test");
                Mockito.when(exception.getOAuth2errorCode()).thenReturn(OAuth2Error.INVALID_REQUEST_CODE);

                RestResponse<Object> response = exceptionMapper.mapAuthorizationErrorException(exception);

                assertEquals("source=oneid&state=old&error=invalid_request&error_description=test",
                                response.getLocation().getRawQuery());
        }

        @Test
        @DisplayName("Malformed callbacks are not interpreted as URI templates")
        void given_callback_template_when_mapping_then_fallback_locally() {
                AuthorizationErrorException exception = Mockito.mock(AuthorizationErrorException.class);
                Mockito.when(exception.getCallbackUri()).thenReturn(
                                "https://client.example/callback?source={oauthState}");
                Mockito.when(exception.getState()).thenReturn("saved-state");
                Mockito.when(exception.getErrorMessage()).thenReturn("test");
                Mockito.when(exception.getOAuth2errorCode()).thenReturn(OAuth2Error.INVALID_REQUEST_CODE);

                RestResponse<Object> response = exceptionMapper.mapAuthorizationErrorException(exception);

                assertEquals("/login/error", response.getLocation().getPath());
                assertEquals("error_code=AUTHORIZATION_ERROR", response.getLocation().getRawQuery());
        }

        @Test
        @DisplayName("A wrapped cookie rejection keeps the same contextual 302 redirect")
        void givenWrappedGenericHtmlError_whenMappingValidationException_thenPreserveContext() {
                GenericHTMLException exception = new GenericHTMLException(ErrorCode.GENERIC_HTML_ERROR,
                                "https://client.example/callback", "saved-state", "client-id");

                RestResponse<Object> response = exceptionMapper.mapValidationException(
                                new ValidationException(exception));

                assertEquals(FOUND.getStatusCode(), response.getStatus());
                assertEquals(exceptionMapper.mapGenericHTMLException(exception).getLocation(),
                                response.getLocation());
        }

        // Only this method, between the ones that use genericHTMLError, will trigger
        // the URISyntaxException
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
                Mockito.when(authorizationErrorExceptionMock.getCallbackUri()).thenReturn(DEFAULT_FALLBACK_URI);
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

                // Path
                Path pathMock = Mockito.mock(Path.class);
                Mockito.when(pathMock.toString()).thenReturn("test");

                // Path.Node
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

                // Path
                Path pathMock = Mockito.mock(Path.class);
                Mockito.when(pathMock.toString()).thenReturn("test");

                // Path.Node
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

                // Path
                Path pathMock = Mockito.mock(Path.class);
                Mockito.when(pathMock.toString()).thenReturn("test");

                // Path.Node
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
                Mockito.when(exceptionMock.getErrorCode()).thenReturn(DEFAULT_ERROR_CODE);
                RestResponse<Object> restResponse = exceptionMapper.mapSAMLResponseStatusException(
                                exceptionMock);
                // then
                checkErrorWithGenericHTMLError(FOUND, exceptionMock.getErrorCode(), restResponse);
        }

        @Test
        void mapSAMLResponseStatusException_withDirectRedirect() {
                SAMLResponseStatusException exceptionMock = Mockito.mock(SAMLResponseStatusException.class);
                Mockito.when(exceptionMock.getMessage()).thenReturn(DETAIL_MESSAGE);
                Mockito.when(exceptionMock.getClientId()).thenReturn(DEFAULT_CLIENT_ID);
                Mockito.when(exceptionMock.getErrorCode()).thenReturn("22");
                URI directRedirect = URI.create(
                                "https://client.example/callback?error=access_denied&error_description=22&state=test");
                Mockito.when(samlErrorRedirectService.resolveRedirect(exceptionMock))
                                .thenReturn(Optional.of(directRedirect));

                RestResponse<Object> restResponse = exceptionMapper.mapSAMLResponseStatusException(
                                exceptionMock);

                assertEquals(FOUND.getStatusCode(), restResponse.getStatus());
                assertEquals(directRedirect, restResponse.getLocation());
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
                Mockito.when(exceptionMock.getCallbackUri()).thenReturn("https://client.example.com/callback");
                Mockito.when(exceptionMock.getErrorMessage()).thenReturn("IDP SSO endpoint not found");
                Mockito.when(exceptionMock.getOAuth2errorCode()).thenReturn("invalid_request");
                Mockito.when(exceptionMock.getState()).thenReturn("dummyState");
                // when
                RestResponse<Object> restResponse = exceptionMapper.mapIDPSSOEndpointNotFoundException(
                                exceptionMock);
                // then
                checkErrorWithAuthenticationErrorResponse(FOUND, "invalid_request", restResponse);
                assertTrue(restResponse.getLocation().toString().contains("state=dummyState"));
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
                Mockito.when(exceptionMock.getCallbackUri()).thenReturn(DEFAULT_FALLBACK_URI);
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
                Mockito.when(exceptionMock.getCallbackUri()).thenReturn(DEFAULT_FALLBACK_URI);
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

        // Only this method, between the ones that use authenticationErrorResponse, will
        // trigger the URISyntaxException
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
                Mockito.when(exceptionMock.getCallbackUri()).thenReturn(DEFAULT_FALLBACK_URI);
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
                Mockito.when(exceptionMock.getCallbackUri()).thenReturn(DEFAULT_FALLBACK_URI);
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
                Mockito.when(exceptionMock.getCallbackUri()).thenReturn(DEFAULT_FALLBACK_URI);
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
                Mockito.when(exceptionMock.getCallbackUri()).thenReturn(DEFAULT_FALLBACK_URI);
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
                RestResponse<TokenRequestErrorDTO> restResponse = exceptionMapper
                                .mapInvalidRequestMalformedHeaderException(
                                                exceptionMock);
                // then
                checkErrorWithBuildTokenRequestErrorDTO(message, BAD_REQUEST, restResponse);
        }

        @Test
        void mapInvalidAccessTokenException() {
                String error = "invalid_token";
                String message = "The access token is invalid or expired.";
                InvalidAccessTokenException exceptionMock = Mockito.mock(InvalidAccessTokenException.class);
                Mockito.when(exceptionMock.getMessage()).thenReturn(error);

                RestResponse<Object> restResponse = exceptionMapper.mapInvalidAccessTokenException(
                                exceptionMock);

                assertEquals(MediaType.APPLICATION_JSON_TYPE, restResponse.getMediaType());
                assertTrue(restResponse.getHeaders().containsKey("WWW-Authenticate"));
                assertTrue(restResponse.getHeaders().get("WWW-Authenticate").toString().contains(
                                "Bearer error=\"invalid_token\""));
                checkErrorWithBuildTokenRequestErrorDTO(error, message, UNAUTHORIZED, restResponse);
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

        // region private methods
        private void checkErrorWithBuildTokenRequestErrorDTO(String message, Status status,
                        RestResponse<TokenRequestErrorDTO> restResponse) {
                checkErrorWithBuildTokenRequestErrorDTO(DETAIL_MESSAGE, message, status, restResponse);
        }

        private void checkErrorWithBuildTokenRequestErrorDTO(String error, String message,
                        Status status, RestResponse<?> restResponse) {
                assertNotNull(restResponse);
                assertEquals(status.getStatusCode(), restResponse.getStatus());
                assertNotNull(restResponse.getEntity());
                assertTrue(restResponse.getEntity() instanceof TokenRequestErrorDTO);
                TokenRequestErrorDTO entity = (TokenRequestErrorDTO) restResponse.getEntity();
                assertEquals(error, entity.getError());
                assertEquals(message, entity.getErrorDescription());
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
        // endregion
}
