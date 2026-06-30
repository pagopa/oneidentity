package it.pagopa.oneid.web.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.groups.ValidationGroups.Registration;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SafeUriValidatorTest {

  @Inject
  Validator validator;

  @Test
  void validateUris_withValidValues_isValid() {
    ClientRegistrationDTO dto = buildValidDto();

    Set<ConstraintViolation<ClientRegistrationDTO>> violations =
        validator.validate(dto, Registration.class);

    assertFalse(hasViolation(violations, "redirectUris"));
    assertFalse(hasViolation(violations, "logoUri"));
    assertFalse(hasViolation(violations, "policyUri"));
    assertFalse(hasViolation(violations, "tosUri"));
    assertFalse(hasViolation(violations, "a11yUri"));
  }

  @Test
  void validateRedirectUris_withInvalidValue_isInvalid() {
    ClientRegistrationDTO dto = buildValidDto();
    dto.setRedirectUris(Set.of(".error"));

    Set<ConstraintViolation<ClientRegistrationDTO>> violations =
        validator.validate(dto, Registration.class);

    assertTrue(hasViolation(violations, "redirectUris"));
  }

  @Test
  void validateLogoUri_withDangerousProtocol_isInvalid() {
    ClientRegistrationDTO dto = buildValidDto();
    dto.setLogoUri("javascript:alert(1)");

    Set<ConstraintViolation<ClientRegistrationDTO>> violations =
        validator.validate(dto, Registration.class);

    assertTrue(hasViolation(violations, "logoUri"));
  }

  private ClientRegistrationDTO buildValidDto() {
    return ClientRegistrationDTO.builder()
        .redirectUris(Set.of("https://test.com"))
        .clientName("client_name_01")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .logoUri("https://logo.example.com")
        .policyUri("https://policy.example.com")
        .tosUri("https://tos.example.com")
        .a11yUri("https://a11y.example.com")
        .build();
  }

  private boolean hasViolation(Set<ConstraintViolation<ClientRegistrationDTO>> violations,
      String fieldName) {
    return violations.stream().anyMatch(v -> fieldName.equals(v.getPropertyPath().toString()));
  }
}
