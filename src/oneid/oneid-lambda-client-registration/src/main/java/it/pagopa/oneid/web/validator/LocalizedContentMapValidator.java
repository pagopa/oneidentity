package it.pagopa.oneid.web.validator;

import static it.pagopa.oneid.service.utils.ValidationUtils.isSafeDescription;
import static it.pagopa.oneid.service.utils.ValidationUtils.isSafeTitle;
import it.pagopa.oneid.common.model.Client.LocalizedContent;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.service.utils.CustomURIUtils;
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

        if (content==null) {
          return false;
        }

        boolean contentValid = isSafeTitle(content.title(), 10)
            && isSafeDescription(content.description());
        
        try {
          if (content.cookieUri()!=null && StringUtils.isNotBlank(content.cookieUri())) {
            CustomURIUtils.validateURI(content.cookieUri());
          }
          if (content.docUri()!=null && StringUtils.isNotBlank(content.docUri())) {
            CustomURIUtils.validateHttpsOrMail(content.docUri());
          }
          if (content.supportAddress()!=null && StringUtils.isNotBlank(content.supportAddress())) {
            CustomURIUtils.validateEmail(content.supportAddress());
          }
        } catch (InvalidUriException e) {
          return false;
        }

        return langValid && contentValid;
      });
    });
  }
}
