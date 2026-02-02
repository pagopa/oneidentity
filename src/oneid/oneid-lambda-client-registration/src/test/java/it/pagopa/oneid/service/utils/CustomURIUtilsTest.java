package it.pagopa.oneid.service.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.model.enums.ClientRegistrationErrorCode;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CustomURIUtilsTest {

  // validateURI tests
  @Test
  void testValidateURI_WithNullUri() {
    Exception exception = assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateURI(null));
    assertEquals(ClientRegistrationErrorCode.EMPTY_URI.getErrorMessage(),
        exception.getMessage());
  }

  @Test
  void testValidateURI_WithEmptyUri() {
    Exception exception = assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateURI(""));
    assertEquals(ClientRegistrationErrorCode.EMPTY_URI.getErrorMessage(),
        exception.getMessage());
  }

  @Test
  void testValidateURI_WithInvalidScheme() {
    Exception exception = assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateURI("ftp://example.com"));
    assertEquals(ClientRegistrationErrorCode.INVALID_URI.getErrorMessage(),
        exception.getMessage());
  }

  @Test
  void testValidateURI_WithInvalidDomain() {
    Exception exception = assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateURI("http://invalid_domain"));
    assertEquals(ClientRegistrationErrorCode.INVALID_URI.getErrorMessage(),
        exception.getMessage());
  }

  @Test
  void testValidateURI_WithValidUri() {
    assertDoesNotThrow(() -> CustomURIUtils.validateURI("https://example.com"));
  }

  @Test
  void testValidateURI_WithValidUriHttps() {
    assertDoesNotThrow(() -> CustomURIUtils.validateURI("https://example.com"));
  }

  @Test
  void testValidateURI_WithSubdomain() {
    assertDoesNotThrow(() -> CustomURIUtils.validateURI("https://sub.example.com"));
  }

  @Test
  void testValidateURI_WithDomainWithNumbers() {
    assertDoesNotThrow(() -> CustomURIUtils.validateURI("https://example123.com"));
  }

  @Test
  void testValidateURI_WithDomainWithHttp() {
    assertThrows(InvalidUriException.class, () -> CustomURIUtils.validateURI("http://example.com"));
  }

  @Test
  void testValidateURI_WithDomainWithSpecialChars() {
    assertThrows(InvalidUriException.class, () -> CustomURIUtils.validateURI("https://example@com"));
  }

  @Test
  void testValidateURI_IPEndpoint() {
    assertThrows(InvalidUriException.class, () -> CustomURIUtils.validateURI("https://127.0.0.1:8080"));
  }

  @Test
  void testValidateURI_NoHttpSchema() {
    assertThrows(InvalidUriException.class, () -> CustomURIUtils.validateURI("javascript:alert()"));
  }

  // validateHttpsOrMail tests
  @Test
  void testValidateHttpsOrMail_WithNullUri() {
    Exception exception = assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateHttpsOrMail(null));
    assertEquals(ClientRegistrationErrorCode.EMPTY_URI.getErrorMessage(),
        exception.getMessage());
  }

  @Test
  void testValidateHttpsOrMail_WithValidHttpsUrl() {
    assertDoesNotThrow(() -> CustomURIUtils.validateHttpsOrMail("https://example.com"));
  }

  @Test
  void testValidateHttpsOrMail_WithValidEmail() {
    assertDoesNotThrow(() -> CustomURIUtils.validateHttpsOrMail("user@example.com"));
  }

  @Test
  void testValidateHttpsOrMail_WithValidEmailWithPlus() {
    assertDoesNotThrow(() -> CustomURIUtils.validateHttpsOrMail("user+tag@example.com"));
  }

  @Test
  void testValidateHttpsOrMail_WithInvalidEmail() {
    assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateHttpsOrMail("invalid-email"));
  }

  @Test
  void testValidateHttpsOrMail_WithHttpUrl() {
    assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateHttpsOrMail("http://example.com"));
  }

  @Test
  void testValidateHttpsOrMail_WithDangerousScheme() {
    assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateHttpsOrMail("javascript:alert()"));
  }

  // validateEmail tests
  @Test
  void testValidateEmail_WithNullEmail() {
    Exception exception = assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateEmail(null));
    assertEquals(ClientRegistrationErrorCode.EMPTY_URI.getErrorMessage(),
        exception.getMessage());
  }

  @Test
  void testValidateEmail_WithValidEmail() {
    assertDoesNotThrow(() -> CustomURIUtils.validateEmail("user@example.com"));
  }

  @Test
  void testValidateEmail_WithValidEmailWithSpecialChars() {
    assertDoesNotThrow(() -> CustomURIUtils.validateEmail("user.name+tag@example.co.it"));
  }

  @Test
  void testValidateEmail_WithInvalidEmail() {
    assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateEmail("invalid-email"));
  }

  @Test
  void testValidateEmail_WithDangerousScheme() {
    assertThrows(InvalidUriException.class,
        () -> CustomURIUtils.validateEmail("javascript:alert()"));
  }
}
