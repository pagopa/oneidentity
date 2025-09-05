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
      Map<String, LocalizedContent> langMap = themeEntry.getValue();
      if (langMap == null) {
        return true; // allow theme removal
      }
      if (langMap.isEmpty()) {
        return false;
      }

      return langMap.entrySet().stream().allMatch(langEntry -> {
        String lang = langEntry.getKey();
        if (lang == null || !ALLOWED_LANGS.contains(lang)) {
          return false;
        }

        LocalizedContent content = langEntry.getValue();
        if (content == null) {
          return true; // allow language removal
        }

        return StringUtils.isNotBlank(content.title())
            && StringUtils.isNotBlank(content.description());
      });
    });

  }
}














