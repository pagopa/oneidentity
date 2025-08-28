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

    final Set<String> allowedLangs = Set.of("IT", "FR", "DE", "SL", "EN");

    for (Map.Entry<String, Map<String, LocalizedContent>> langEntry : map.entrySet()) {
      String language = langEntry.getKey();
      Map<String, LocalizedContent> themesMap = langEntry.getValue();

      if (language == null || !allowedLangs.contains(language)) {
        return false;
      }

      if (themesMap == null) {
        return false;
      }

      for (Map.Entry<String, LocalizedContent> themeEntry : themesMap.entrySet()) {
        LocalizedContent content = themeEntry.getValue();

        if (content == null) {
          return false;
        }
        if (StringUtils.isBlank(content.title()) || StringUtils.isBlank(content.description())
            || StringUtils.isBlank(content.docUri()) || StringUtils.isBlank(
            content.supportAddress()) || StringUtils.isBlank(content.cookieUri())
        ) {
          return false;
        }

      }
    }

    return true;
  }
}
