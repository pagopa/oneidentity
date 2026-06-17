package it.pagopa.oneid.web.validator;

import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.service.utils.CustomURIUtils;
import it.pagopa.oneid.web.validator.annotations.SafeUriCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class SafeUriValidator implements ConstraintValidator<SafeUriCheck, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
    if (StringUtils.isBlank(value)) {
      return true;
    }
    try {
      CustomURIUtils.validateURI(value);
      return true;
    } catch (InvalidUriException e) {
      return false;
    }
  }
}