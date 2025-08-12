package it.pagopa.oneid.web.validator;

import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.web.validator.annotations.SamlRequestedAttributeCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class SamlRequestedAttributeValidator implements
    ConstraintValidator<SamlRequestedAttributeCheck, Set<String>> {


  @Override
  public void initialize(SamlRequestedAttributeCheck constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(Set<String> identifiers,
      ConstraintValidatorContext constraintValidatorContext) {
    if (identifiers == null || identifiers.isEmpty()) {
      return true;
    }
    for (String identifier : identifiers) {
      if (identifier == null) {
        return false;
      }
      try {
        Identifier.valueOf(identifier);
      } catch (IllegalArgumentException e) {
        return false;
      }
    }
    return true;
  }

}

