package it.pagopa.oneid.web.validator;

import static it.pagopa.oneid.service.utils.ValidationUtils.isSafeDescription;
import static it.pagopa.oneid.service.utils.ValidationUtils.isSafeTitle;
import it.pagopa.oneid.common.model.Client.LocalizedContent;
import it.pagopa.oneid.web.validator.annotations.LocalizedContentMapCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Set;

public class LocalizedContentMapValidator implements
    ConstraintValidator<LocalizedContentMapCheck, Map<String, Map<String, LocalizedContent>>> {

  private static final Set<String> ALLOWED_LANGS = Set.of("it", "fr", "de", "sl", "en");
  private final SafeUriValidator safeUriValidator = new SafeUriValidator();
  private final SafeHttpsOrMailValidator safeHttpsOrMailValidator = new SafeHttpsOrMailValidator();
  private final SafeEmailValidator safeEmailValidator = new SafeEmailValidator();

  @Override
  public void initialize(LocalizedContentMapCheck constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(Map<String, Map<String, LocalizedContent>> map,
      ConstraintValidatorContext constraintValidatorContext) {

    if (map == null) {
      return true;
    }
    if (map.isEmpty()) {
      return false;
    }

    return map.entrySet().stream().allMatch(themeEntry -> {
      String theme = themeEntry.getKey();
      Map<String, LocalizedContent> langMap = themeEntry.getValue();

      boolean themeValid = theme != null && !theme.isEmpty();
      boolean langMapValid = langMap != null && !langMap.isEmpty();

      return themeValid
          && langMapValid
          && langMap.entrySet().stream().allMatch(langEntry -> {

        String lang = langEntry.getKey();
        LocalizedContent content = langEntry.getValue();

        boolean langValid = lang != null && ALLOWED_LANGS.contains(lang);

        if (content==null) {
          return false;
        }

        boolean contentValid = isSafeTitle(content.title(), 10)
            && isSafeDescription(content.description());

        boolean cookieUriValid = safeUriValidator.isValid(content.cookieUri(),
            constraintValidatorContext);
        boolean docUriValid = safeHttpsOrMailValidator.isValid(content.docUri(),
            constraintValidatorContext);
        boolean supportAddressValid = safeEmailValidator.isValid(content.supportAddress(),
            constraintValidatorContext);

        return langValid && contentValid && cookieUriValid && docUriValid && supportAddressValid;
      });
    });
  }
}
