package it.pagopa.oneid.web.validator;

import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.web.validator.annotations.AuthLevelCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AuthLevelValidator implements
    ConstraintValidator<AuthLevelCheck, String> {

  @Override
  public void initialize(AuthLevelCheck constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String authLevel, ConstraintValidatorContext constraintValidatorContext) {
    if (authLevel == null) {
      return true;
    }
    
    return AuthLevel.authLevelFromValue(authLevel) != null;
  }
}
