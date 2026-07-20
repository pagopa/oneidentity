package it.pagopa.oneid.web.validator;

import com.nimbusds.oauth2.sdk.client.RedirectURIValidator;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.service.utils.CustomURIUtils;
import it.pagopa.oneid.web.validator.annotations.SafeRedirectUrisCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.util.Set;

public class SafeRedirectUrisValidator
    implements ConstraintValidator<SafeRedirectUrisCheck, Set<String>> {

  @Override
  public boolean isValid(Set<String> values, ConstraintValidatorContext constraintValidatorContext) {
    if (values == null) {
      return true;
    }
    if (values.isEmpty()) {
      return false;
    }

    for (String redirectUri : values) {
      try {
        CustomURIUtils.validateURI(redirectUri);
        RedirectURIValidator.ensureLegal(URI.create(redirectUri));
      } catch (IllegalArgumentException | NullPointerException | InvalidUriException e) {
        return false;
      }
    }
    return true;
  }
}
