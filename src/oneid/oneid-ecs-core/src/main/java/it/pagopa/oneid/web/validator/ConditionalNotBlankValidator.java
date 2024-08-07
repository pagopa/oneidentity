package it.pagopa.oneid.web.validator;

import io.quarkus.logging.Log;
import io.quarkus.runtime.configuration.ConfigUtils;
import it.pagopa.oneid.web.validator.annotations.ConditionalNotBlank;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class ConditionalNotBlankValidator implements
    ConstraintValidator<ConditionalNotBlank, String> {

  String field;

  @Override
  public void initialize(ConditionalNotBlank constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
    this.field = constraintAnnotation.field();
  }

  @Override
  public boolean isValid(String o, ConstraintValidatorContext constraintValidatorContext) {
    if (!ConfigUtils.getProfiles().contains("dev")) {
      if (o == null || StringUtils.isBlank(o)) {
        Log.error(this.field + " must not be blank");
        return false;
      }
    }
    return true;
  }
}
