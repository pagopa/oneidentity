package it.pagopa.oneid.web.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.AuthorizationErrorException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedGet;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedPost;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AuthenticationRequestValidatorTest {

  private AuthenticationRequestValidator validator;
  private Map<String, Client> clientsMap;

  @BeforeEach
  void setUp() {
    validator = new AuthenticationRequestValidator();
    clientsMap = new HashMap<>();
    validator.clientsMap = clientsMap;
  }

  @Test
  void testValidGetRequest() {
    AuthorizationRequestDTOExtendedGet request = mock(AuthorizationRequestDTOExtendedGet.class);
    when(request.getResponseType()).thenReturn(ResponseType.CODE);
    when(request.getRedirectUri()).thenReturn("http://callback");
    when(request.getClientId()).thenReturn("client1");
    when(request.getState()).thenReturn("state");
    when(request.getIdp()).thenReturn("idp1");

    Client client = mock(Client.class);
    when(client.getCallbackURI()).thenReturn(Set.of("http://callback"));
    clientsMap.put("client1", client);

    assertTrue(validator.isValid(request, mock(ConstraintValidatorContext.class)));
  }

  @Test
  void testValidPostRequest() {
    AuthorizationRequestDTOExtendedPost request = mock(AuthorizationRequestDTOExtendedPost.class);
    when(request.getResponseType()).thenReturn(ResponseType.CODE);
    when(request.getRedirectUri()).thenReturn("http://callback");
    when(request.getClientId()).thenReturn("client2");
    when(request.getState()).thenReturn("state");
    when(request.getIdp()).thenReturn("idp1");

    Client client = mock(Client.class);
    when(client.getCallbackURI()).thenReturn(Set.of("http://callback"));
    clientsMap.put("client2", client);

    assertTrue(validator.isValid(request, mock(ConstraintValidatorContext.class)));
  }

  @Test
  void testInvalidResponseTypeWithValidCallback() {
    AuthorizationRequestDTOExtendedGet request = mock(AuthorizationRequestDTOExtendedGet.class);
    when(request.getResponseType()).thenReturn(null);
    when(request.getRedirectUri()).thenReturn("http://callback");
    when(request.getClientId()).thenReturn("client1");
    when(request.getState()).thenReturn("state");
    when(request.getIdp()).thenReturn("idp1");

    Client client = mock(Client.class);
    when(client.getCallbackURI()).thenReturn(Set.of("http://callback"));
    clientsMap.put("client1", client);

    Exception exception = assertThrows(AuthorizationErrorException.class,
        () -> validator.isValid(request, mock(ConstraintValidatorContext.class)));

    assertEquals(ErrorCode.AUTHORIZATION_ERROR_RESPONSE_TYPE.getErrorMessage(),
        exception.getMessage());
  }

  @Test
  void testInvalidIdpWithValidCallback() {
    AuthorizationRequestDTOExtendedGet request = mock(AuthorizationRequestDTOExtendedGet.class);
    when(request.getResponseType()).thenReturn(ResponseType.CODE);
    when(request.getRedirectUri()).thenReturn("http://callback");
    when(request.getClientId()).thenReturn("client1");
    when(request.getState()).thenReturn("state");
    when(request.getIdp()).thenReturn(null);

    Client client = mock(Client.class);
    when(client.getCallbackURI()).thenReturn(Set.of("http://callback"));
    clientsMap.put("client1", client);

    Exception exception = assertThrows(AuthorizationErrorException.class,
        () -> validator.isValid(request, mock(ConstraintValidatorContext.class)));

    assertEquals(ErrorCode.AUTHORIZATION_ERROR_IDP.getErrorMessage(), exception.getMessage());
  }

  @Test
  void testInvalidIdpWithInvalidClient() {
    AuthorizationRequestDTOExtendedGet request = mock(AuthorizationRequestDTOExtendedGet.class);
    when(request.getResponseType()).thenReturn(ResponseType.CODE);
    when(request.getRedirectUri()).thenReturn("http://callback");
    when(request.getClientId()).thenReturn("client3");
    when(request.getState()).thenReturn("state");
    when(request.getIdp()).thenReturn(null);

    Client client = mock(Client.class);
    when(client.getCallbackURI()).thenReturn(Set.of("http://callback"));
    clientsMap.put("client1", client);

    Exception exception = assertThrows(GenericHTMLException.class,
        () -> validator.isValid(request, mock(ConstraintValidatorContext.class)));

    assertEquals(ErrorCode.AUTHORIZATION_ERROR_IDP, ErrorCode.valueOf(exception.getMessage()));
  }

  @Test
  void testInvalidRequestTypeThrowsException() {
    Exception exception = assertThrows(GenericHTMLException.class,
        () -> validator.isValid(new Object(), mock(ConstraintValidatorContext.class)));

    assertEquals(ErrorCode.GENERIC_HTML_ERROR, ErrorCode.valueOf(exception.getMessage()));
  }

  @Test
  void testNullCallbackUriAndClient() {
    AuthorizationRequestDTOExtendedGet request = mock(AuthorizationRequestDTOExtendedGet.class);
    when(request.getResponseType()).thenReturn(null);
    when(request.getRedirectUri()).thenReturn(null);
    when(request.getClientId()).thenReturn("client1");
    when(request.getState()).thenReturn("state");
    when(request.getIdp()).thenReturn(null);

    Exception exception = assertThrows(GenericHTMLException.class,
        () -> validator.isValid(request, mock(ConstraintValidatorContext.class)));

    assertEquals(ErrorCode.AUTHORIZATION_ERROR_RESPONSE_TYPE,
        ErrorCode.valueOf(exception.getMessage()));
  }

  // --- assertionRef format validation ---

  private AuthorizationRequestDTOExtendedGet validGetRequestWithAssertionRef(String assertionRef) {
    AuthorizationRequestDTOExtendedGet request = mock(AuthorizationRequestDTOExtendedGet.class);
    when(request.getResponseType()).thenReturn(ResponseType.CODE);
    when(request.getRedirectUri()).thenReturn("http://callback");
    when(request.getClientId()).thenReturn("client1");
    when(request.getState()).thenReturn("state");
    when(request.getIdp()).thenReturn("idp1");
    when(request.getAssertionRef()).thenReturn(assertionRef);
    Client client = mock(Client.class);
    when(client.getCallbackURI()).thenReturn(Set.of("http://callback"));
    clientsMap.put("client1", client);
    return request;
  }

  @Test
  @DisplayName("given valid sha256 assertion-ref, when isValid, then no exception")
  void assertionRef_validSha256() {
    assertTrue(validator.isValid(
        validGetRequestWithAssertionRef("sha256-3f2a9c7f4b1d8e6c5a2f9b7d4c1e8a6f3"),
        mock(ConstraintValidatorContext.class)));
  }

  @Test
  @DisplayName("given valid sha384 assertion-ref, when isValid, then no exception")
  void assertionRef_validSha384() {
    assertTrue(validator.isValid(
        validGetRequestWithAssertionRef("sha384-abcDEF123-_xyz"),
        mock(ConstraintValidatorContext.class)));
  }

  @Test
  @DisplayName("given valid sha512 assertion-ref, when isValid, then no exception")
  void assertionRef_validSha512() {
    assertTrue(validator.isValid(
        validGetRequestWithAssertionRef("sha512-AABBCCDD1122"),
        mock(ConstraintValidatorContext.class)));
  }

  @Test
  @DisplayName("given unsupported algorithm prefix, when isValid, then AuthorizationErrorException")
  void assertionRef_invalidAlgorithm() {
    assertThrows(AuthorizationErrorException.class, () -> validator.isValid(
        validGetRequestWithAssertionRef("md5-3f2a9c7f4b1d8e6c5a2f9b7d4c1e8a6f3"),
        mock(ConstraintValidatorContext.class)));
  }

  @Test
  @DisplayName("given missing thumbprint after dash, when isValid, then AuthorizationErrorException")
  void assertionRef_missingThumbprint() {
    assertThrows(AuthorizationErrorException.class, () -> validator.isValid(
        validGetRequestWithAssertionRef("sha256-"),
        mock(ConstraintValidatorContext.class)));
  }

  @Test
  @DisplayName("given value exceeding max length 160 chars, when isValid, then AuthorizationErrorException")
  void assertionRef_exceedsMaxLength() {
    String longValue = "sha256-" + "a".repeat(154);
    assertThrows(AuthorizationErrorException.class, () -> validator.isValid(
        validGetRequestWithAssertionRef(longValue),
        mock(ConstraintValidatorContext.class)));
  }

  @Test
  @DisplayName("given colon in value, when isValid, then AuthorizationErrorException")
  void assertionRef_colonNotAllowed() {
    assertThrows(AuthorizationErrorException.class, () -> validator.isValid(
        validGetRequestWithAssertionRef("sha256-abc:def"),
        mock(ConstraintValidatorContext.class)));
  }

  @Test
  @DisplayName("given null assertion-ref, when isValid, then no exception")
  void assertionRef_null_skipsValidation() {
    assertTrue(validator.isValid(
        validGetRequestWithAssertionRef(null),
        mock(ConstraintValidatorContext.class)));
  }
}
