package it.pagopa.oneid.web.validator;

import it.pagopa.oneid.common.model.Client.LocalizedContent;
import it.pagopa.oneid.web.validator.annotations.LocalizedContentMapCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
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
      if (theme == null || theme.isEmpty()) {
        return false;
      }

      Map<String, LocalizedContent> langMap = themeEntry.getValue();
      if (langMap == null || langMap.isEmpty()) {
        return false;
      }

      return langMap.entrySet().stream().allMatch(langEntry -> {
        String lang = langEntry.getKey();
        if (lang == null || !ALLOWED_LANGS.contains(lang)) {
          return false;
        }

        LocalizedContent content = langEntry.getValue();
        if (content == null) {
          return false;
        }

        return Stream.of(
            content.title(),
            content.description()
        ).noneMatch(StringUtils::isBlank)
            && content.title().length() >= 10
            && content.description().length() >= 20;
      });
    });
  }
}
