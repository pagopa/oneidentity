package it.pagopa.oneid.web.validator;

import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.web.validator.annotations.AuthLevelCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class AuthLevelValidator implements
    ConstraintValidator<AuthLevelCheck, Set<String>> {


  @Override
  public void initialize(AuthLevelCheck constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(Set<String> acr, ConstraintValidatorContext constraintValidatorContext) {

    if (acr == null) {
      return true;
    }
    if (acr.isEmpty()) {
      return false;
    }
    return acr.stream().noneMatch(authLevel -> AuthLevel.authLevelFromValue(authLevel) == null);

  }
}
