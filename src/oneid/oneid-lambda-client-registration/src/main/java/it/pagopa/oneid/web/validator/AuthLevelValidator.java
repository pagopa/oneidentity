package it.pagopa.oneid.web.validator;

import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.web.validator.annotations.AuthLevelCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class AuthLevelValidator implements
    ConstraintValidator<AuthLevelCheck, List<String>> {


  @Override
  public void initialize(AuthLevelCheck constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(List<String> acr, ConstraintValidatorContext constraintValidatorContext) {

    if (acr != null && !acr.isEmpty()) {
      return acr.stream().noneMatch(authLevel -> AuthLevel.authLevelFromValue(authLevel) == null);
    }
    return false;

  }
}
