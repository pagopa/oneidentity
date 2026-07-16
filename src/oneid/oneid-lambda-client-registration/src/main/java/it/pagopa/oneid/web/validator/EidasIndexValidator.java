package it.pagopa.oneid.web.validator;

import it.pagopa.oneid.web.validator.annotations.EidasIndexCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EidasIndexValidator implements ConstraintValidator<EidasIndexCheck, Integer> {

  private static final int EIDAS_INDEX_99 = 99;
  private static final int EIDAS_INDEX_100 = 100;

  @Override
  public void initialize(EidasIndexCheck constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(Integer eidasIndex, ConstraintValidatorContext constraintValidatorContext) {
    if (eidasIndex == null) {
      return true;
    }
    return eidasIndex == EIDAS_INDEX_99 || eidasIndex == EIDAS_INDEX_100;
  }
}
