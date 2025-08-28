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

  private static final Set<String> ALLOWED_LANGS = Set.of("IT", "FR", "DE", "SL", "EN");

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

    return map.entrySet().stream().allMatch(langEntry -> {
      String lang = langEntry.getKey();
      if (lang == null || !ALLOWED_LANGS.contains(lang)) {
        return false;
      }

      Map<String, LocalizedContent> themes = langEntry.getValue();
      if (themes == null || themes.isEmpty()) {
        return false;
      }

      return themes.values().stream().allMatch(content ->
          content != null &&
              Stream.of(
                  content.title(),
                  content.description(),
                  content.docUri(),
                  content.supportAddress(),
                  content.cookieUri()
              ).noneMatch(StringUtils::isBlank)
      );
    });
  }
}
