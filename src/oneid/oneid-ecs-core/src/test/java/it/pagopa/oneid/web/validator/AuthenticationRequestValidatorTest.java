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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

    assertTrue(validator.isValid(request, Mockito.mock(ConstraintValidatorContext.class)));
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

    assertTrue(validator.isValid(request, Mockito.mock(ConstraintValidatorContext.class)));
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
        () -> validator.isValid(request, Mockito.mock(ConstraintValidatorContext.class)));

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
        () -> validator.isValid(request, Mockito.mock(ConstraintValidatorContext.class)));

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
        () -> validator.isValid(request, Mockito.mock(ConstraintValidatorContext.class)));

    assertEquals(ErrorCode.AUTHORIZATION_ERROR_IDP, ErrorCode.valueOf(exception.getMessage()));
  }

  @Test
  void testInvalidRequestTypeThrowsException() {
    Exception exception = assertThrows(GenericHTMLException.class,
        () -> validator.isValid(new Object(), Mockito.mock(ConstraintValidatorContext.class)));

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
        () -> validator.isValid(request, Mockito.mock(ConstraintValidatorContext.class)));

    assertEquals(ErrorCode.AUTHORIZATION_ERROR_RESPONSE_TYPE,
        ErrorCode.valueOf(exception.getMessage()));
  }
}
