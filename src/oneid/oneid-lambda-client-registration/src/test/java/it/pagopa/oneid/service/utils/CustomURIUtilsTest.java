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
    assertDoesNotThrow(() -> CustomURIUtils.validateURI("http://example.com"));
  }

  @Test
  void testValidateURI_WithValidUriHttps() {
    assertDoesNotThrow(() -> CustomURIUtils.validateURI("https://example.com"));
  }

  @Test
  void testValidateURI_WithSubdomain() {
    assertDoesNotThrow(() -> CustomURIUtils.validateURI("http://sub.example.com"));
  }

  @Test
  void testValidateURI_WithDomainWithNumbers() {
    assertDoesNotThrow(() -> CustomURIUtils.validateURI("http://example123.com"));
  }

  @Test
  void testValidateURI_WithDomainWithSpecialChars() {
    assertThrows(InvalidUriException.class, () -> CustomURIUtils.validateURI("http://example@com"));
  }
}
