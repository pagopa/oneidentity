package it.pagopa.oneid.web.validator;

import it.pagopa.oneid.service.utils.ValidationUtils;
import it.pagopa.oneid.web.validator.annotations.SafeTitleCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SafeTitleValidator implements ConstraintValidator<SafeTitleCheck, String> {

  private Integer minLen;

  @Override
  public void initialize(SafeTitleCheck constraintAnnotation) {
    int configuredMinLen = constraintAnnotation.minLen();
    this.minLen = configuredMinLen > 0 ? configuredMinLen : null;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
    if (value == null) {
      return true;
    }

    return ValidationUtils.isSafeTitle(value, minLen);
  }
}
