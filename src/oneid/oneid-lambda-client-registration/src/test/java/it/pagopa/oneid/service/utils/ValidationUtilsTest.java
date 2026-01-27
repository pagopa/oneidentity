package it.pagopa.oneid.service.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ValidationUtilsTest {

  // isSafeTitle tests (without min/max)
  @Test
  void testIsSafeTitle_WithNull() {
    assertFalse(ValidationUtils.isSafeTitle(null));
  }

  @Test
  void testIsSafeTitle_WithValidTitle() {
    assertTrue(ValidationUtils.isSafeTitle("Valid Title 123"));
  }

  @Test
  void testIsSafeTitle_WithValidTitleWithSpecialChars() {
    assertTrue(ValidationUtils.isSafeTitle("Title with, dots. and 'quotes' and \"double\" (parens) - dash"));
  }

  @Test
  void testIsSafeTitle_WithNewline() {
    assertFalse(ValidationUtils.isSafeTitle("Title with\nnewline"));
  }

  @Test
  void testIsSafeTitle_WithInvalidChars() {
    assertFalse(ValidationUtils.isSafeTitle("Title with @ or #"));
  }

  @Test
  void testIsSafeTitle_WithEmptyString() {
    assertFalse(ValidationUtils.isSafeTitle(""));
  }

  @Test
  void testIsSafeTitle_WithUnicodeLetters() {
    assertTrue(ValidationUtils.isSafeTitle("Title with àèé"));
  }

  // isSafeTitle tests (with min)
  @Test
  void testIsSafeTitleWithMin_WithValidTitle() {
    assertTrue(ValidationUtils.isSafeTitle("Valid Title", 5));
  }

  @Test
  void testIsSafeTitleWithMin_BelowMinLength() {
    assertFalse(ValidationUtils.isSafeTitle("Short", 10));
  }

  @Test
  void testIsSafeTitleWithMin_WithNullMin() {
    assertTrue(ValidationUtils.isSafeTitle("Short", null));
  }

  // isSafeDescription tests
  @Test
  void testIsSafeDescription_WithNull() {
    assertFalse(ValidationUtils.isSafeDescription(null));
  }

  @Test
  void testIsSafeDescription_WithValidDescription() {
    String description = "This is a valid description with at least twenty characters that should pass all checks.";
    assertTrue(ValidationUtils.isSafeDescription(description));
  }

  @Test
  void testIsSafeDescription_BelowMinLength() {
    assertFalse(ValidationUtils.isSafeDescription("Too short"));
  }
  
  @Test
  void testIsSafeDescription_WithNullCharacter() {
    String description = "Valid description with at least twenty chars\u0000 and null";
    assertFalse(ValidationUtils.isSafeDescription(description));
  }

  @Test
  void testIsSafeDescription_WithAngularBrackets() {
    String description = "Valid description with at least twenty chars < and html";
    assertFalse(ValidationUtils.isSafeDescription(description));
  }

  @Test
  void testIsSafeDescription_WithNewlines() {
    String description = "Valid description with\nat least twenty\ncharacters and newlines.";
    assertTrue(ValidationUtils.isSafeDescription(description));
  }

  @Test
  void testIsSafeDescription_WithSpecialCharacters() {
    String description = "Description with special chars: @#$%^&*()_+=[]{}|;:,./? and twenty chars minimum";
    assertTrue(ValidationUtils.isSafeDescription(description));
  }

  @Test
  void testIsSafeDescription_WithUnicode() {
    String description = "Character unicode àèé and at least twenty chars";
    assertTrue(ValidationUtils.isSafeDescription(description));
  }

  @Test
  void testIsSafeDescription_WithDangerousProtocolJavascript() {
    String description = "javascript: protocol and twenty chars";
    assertFalse(ValidationUtils.isSafeDescription(description));
  }

  @Test
  void testIsSafeDescription_WithDangerousProtocolCaseInsensitive() {
    String description = "JaVaScRiPt: protocol and twenty chars";
    assertFalse(ValidationUtils.isSafeDescription(description));
  }

  @Test
  void testIsSafeDescription_WithNormalTextContainingJavascript() {
    String description = "This description talks about javascript programming for at least twenty chars";
    assertTrue(ValidationUtils.isSafeDescription(description));
  }
}

