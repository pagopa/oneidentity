package it.pagopa.oneid.web.controller.utils;

import it.pagopa.oneid.common.model.enums.Identifier;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class RequestedParameterUtilsTest {

  @Test
  void shouldMapValidIdentifiersToFriendlyNames() {
    Set<String> input = Set.of("fiscalNumber", "spidCode");
    Set<String> result = RequestedParameterUtils.mapToFriendlyNames(input);

    assertTrue(result.contains(Identifier.fiscalNumber.getFriendlyName()));
    assertTrue(result.contains(Identifier.spidCode.getFriendlyName()));
  }

  @Test
  void shouldReturnOriginalValueIfIdentifierIsInvalid() {
    Set<String> input = Set.of("unknownKey", "email");
    Set<String> result = RequestedParameterUtils.mapToFriendlyNames(input);

    assertTrue(result.contains("unknownKey")); // fallback
    assertTrue(result.contains(Identifier.email.getFriendlyName()));
  }

  @Test
  void shouldReturnEmptySetIfInputIsEmpty() {
    Set<String> result = RequestedParameterUtils.mapToFriendlyNames(Set.of());
    assertTrue(result.isEmpty());
  }
}