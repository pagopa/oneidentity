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
class SafeTitleValidatorTest {

  @Inject
  Validator validator;

  @Test
  void validateClientName_withUnderscore_isValid() {
    ClientRegistrationDTO dto = buildValidDto("client_name_01");

    Set<ConstraintViolation<ClientRegistrationDTO>> violations =
        validator.validate(dto, Registration.class);

    assertFalse(hasClientNameViolation(violations));
  }

  @Test
  void validateClientName_withDangerousProtocol_isInvalid() {
    ClientRegistrationDTO dto = buildValidDto("javascript:alert(1)");

    Set<ConstraintViolation<ClientRegistrationDTO>> violations =
        validator.validate(dto, Registration.class);

    assertTrue(hasClientNameViolation(violations));
  }

  private ClientRegistrationDTO buildValidDto(String clientName) {
    return ClientRegistrationDTO.builder()
        .redirectUris(Set.of("https://test.com"))
        .clientName(clientName)
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .build();
  }

  private boolean hasClientNameViolation(Set<ConstraintViolation<ClientRegistrationDTO>> violations) {
    return violations.stream().anyMatch(v -> "clientName".equals(v.getPropertyPath().toString()));
  }
}
