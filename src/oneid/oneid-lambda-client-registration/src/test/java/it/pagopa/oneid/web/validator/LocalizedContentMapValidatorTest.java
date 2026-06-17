package it.pagopa.oneid.web.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client.LocalizedContent;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.groups.ValidationGroups.Registration;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class LocalizedContentMapValidatorTest {

  @Inject
  Validator validator;

  @Test
  @DisplayName("given a safe theme name when validating then no violation is raised")
  void given_safe_theme_name_when_validating_then_no_violation_is_raised() {
    ClientRegistrationDTO dto = buildValidDto("default");

    Set<ConstraintViolation<ClientRegistrationDTO>> violations =
        validator.validate(dto, Registration.class);

    assertFalse(hasLocalizedContentMapViolation(violations));
  }

  @Test
  @DisplayName("given a dangerous theme name when validating then a violation is raised")
  void given_dangerous_theme_name_when_validating_then_violation_is_raised() {
    ClientRegistrationDTO dto = buildValidDto("javascript:alert(1)");

    Set<ConstraintViolation<ClientRegistrationDTO>> violations =
        validator.validate(dto, Registration.class);

    assertTrue(hasLocalizedContentMapViolation(violations));
  }

  private ClientRegistrationDTO buildValidDto(String themeName) {
    Map<String, Map<String, LocalizedContent>> localizedContentMap = new HashMap<>();
    localizedContentMap.put(themeName, Map.of(
        "it",
        new LocalizedContent(
            "Title of minimum 10 characters",
            "Description of minimum 20 characters to pass the constraint",
            "https://test.com",
            "support@gmail.com",
            null
        )
    ));

    return ClientRegistrationDTO.builder()
        .redirectUris(Set.of("https://test.com"))
        .clientName("test")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .localizedContentMap(localizedContentMap)
        .build();
  }

  private boolean hasLocalizedContentMapViolation(
      Set<ConstraintViolation<ClientRegistrationDTO>> violations) {
    return violations.stream()
        .anyMatch(v -> "localizedContentMap".equals(v.getPropertyPath().toString()));
  }
}
