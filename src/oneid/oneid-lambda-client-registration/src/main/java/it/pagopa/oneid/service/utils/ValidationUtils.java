package it.pagopa.oneid.service.utils;

import it.pagopa.oneid.common.utils.logging.CustomLogging;
import java.util.regex.Pattern;

@CustomLogging
public class ValidationUtils {
  private static final Pattern DANGEROUS_PROTOCOL_PREFIX =
      Pattern.compile("\\b(?:javascript|data|vbscript)\\s*:", Pattern.CASE_INSENSITIVE);

  private static boolean containsDangerousProtocol(String value) {
    return DANGEROUS_PROTOCOL_PREFIX.matcher(value).find();
  }

  public static boolean isSafeTitle(String value) {
    return isSafeTitle(value, null);
  }

  public static boolean isSafeTitle(String value, Integer minLen) {
    if (value == null) return false;

    String v = value.trim();

    if (v.isEmpty() || (minLen != null && v.length() < minLen)) return false;

    return v.codePoints().noneMatch(Character::isISOControl)
        && v.indexOf('<') < 0
      && v.indexOf('>') < 0
      && !containsDangerousProtocol(v);
  }

  public static boolean isSafeDescription(String value) {
    if (value==null) return false;
    String v = value.trim();
    return (v.length() >= 20)
        && (v.indexOf('\u0000') < 0)
        && (v.indexOf('<') < 0 && v.indexOf('>') < 0)
        && !containsDangerousProtocol(v);
  }
}
