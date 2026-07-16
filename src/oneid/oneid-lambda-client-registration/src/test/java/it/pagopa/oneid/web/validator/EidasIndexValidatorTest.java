package it.pagopa.oneid.web.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EidasIndexValidatorTest {

  private final EidasIndexValidator validator = new EidasIndexValidator();

  @Test
  void isValid_withMissingIndex_returnsTrue() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  void isValid_withMinimumIndex_returnsTrue() {
    assertTrue(validator.isValid(99, null));
  }

  @Test
  void isValid_withCompleteIndex_returnsTrue() {
    assertTrue(validator.isValid(100, null));
  }

  @Test
  void isValid_withUnsupportedIndex_returnsFalse() {
    assertFalse(validator.isValid(101, null));
  }
}
