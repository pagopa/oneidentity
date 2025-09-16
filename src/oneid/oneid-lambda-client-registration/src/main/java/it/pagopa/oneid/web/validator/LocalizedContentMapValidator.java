package it.pagopa.oneid.web.validator;

import it.pagopa.oneid.common.model.Client.LocalizedContent;
import it.pagopa.oneid.web.validator.annotations.LocalizedContentMapCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class LocalizedContentMapValidator implements
    ConstraintValidator<LocalizedContentMapCheck, Map<String, Map<String, LocalizedContent>>> {

  private static final Set<String> ALLOWED_LANGS = Set.of("it", "fr", "de", "sl", "en");

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
        boolean contentValid = content != null
            && !StringUtils.isBlank(content.title())
            && !StringUtils.isBlank(content.description())
            && content.title().length() >= 10
            && content.description().length() >= 20;

        return langValid && contentValid;
      });
    });
  }
}
