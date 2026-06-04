package it.pagopa.oneid.web.validator;

import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.web.validator.annotations.SpidMinorsCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SpidMinorsValidator implements
    ConstraintValidator<SpidMinorsCheck, ClientRegistrationDTO> {

  private static final int MIN_AGE_LOWER_BOUND = 5;
  private static final int MIN_AGE_UPPER_BOUND = 17;
  private static final int MAX_AGE_UPPER_BOUND = 999;
  private static final int AGE_PARENT_AUTH_UPPER_BOUND = 18;

  @Override
  public void initialize(SpidMinorsCheck constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(ClientRegistrationDTO dto,
      ConstraintValidatorContext context) {
    if (dto.getSpidMinors() == null || !dto.getSpidMinors()) {
      return true;
    }

    context.disableDefaultConstraintViolation();

    if (dto.getMinAge() == null) {
      context.buildConstraintViolationWithTemplate("minAge is required when spidMinors is true")
          .addPropertyNode("minAge").addConstraintViolation();
      return false;
    }

    int minAge = dto.getMinAge();
    if (minAge < MIN_AGE_LOWER_BOUND || minAge > MIN_AGE_UPPER_BOUND) {
      context.buildConstraintViolationWithTemplate(
              "minAge must be between " + MIN_AGE_LOWER_BOUND + " and " + MIN_AGE_UPPER_BOUND)
          .addPropertyNode("minAge").addConstraintViolation();
      return false;
    }

    if (dto.getMaxAge() != null) {
      int maxAge = dto.getMaxAge();
      if (maxAge < minAge || maxAge > MAX_AGE_UPPER_BOUND) {
        context.buildConstraintViolationWithTemplate(
                "maxAge must be between minAge and " + MAX_AGE_UPPER_BOUND)
            .addPropertyNode("maxAge").addConstraintViolation();
        return false;
      }
    }

    if (dto.getAgeParentAuth() != null) {
      int ageParentAuth = dto.getAgeParentAuth();
      if (ageParentAuth != 0
          && (ageParentAuth <= minAge || ageParentAuth >= AGE_PARENT_AUTH_UPPER_BOUND)) {
        context.buildConstraintViolationWithTemplate(
                "ageParentAuth must be 0 or greater than minAge and less than "
                    + AGE_PARENT_AUTH_UPPER_BOUND)
            .addPropertyNode("ageParentAuth").addConstraintViolation();
        return false;
      }
    }

    return true;
  }
}
