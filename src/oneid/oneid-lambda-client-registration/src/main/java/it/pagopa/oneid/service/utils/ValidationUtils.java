package it.pagopa.oneid.service.utils;

import it.pagopa.oneid.common.utils.logging.CustomLogging;
import java.util.regex.Pattern;

@CustomLogging
public class ValidationUtils {
  private static final Pattern DANGEROUS_PROTOCOL_PREFIX =
      Pattern.compile("^\\s*(javascript|data|vbscript)\\s*:", Pattern.CASE_INSENSITIVE);

  public static boolean isSafeTitle(String value) {
    return isSafeTitle(value, null, null);
  }

  public static boolean isSafeTitle(String value, Integer minLen, Integer maxLen) {
    if (value == null) return false;

    String v = value.trim();

    if (v.contains("\n") || v.contains("\r") || (minLen != null && v.length() < minLen) || (maxLen != null && v.length() > maxLen)) return false;

    return v.matches("^[\\p{L}\\p{N} .,'’\"()\\-]+$");
  }

  public static boolean isSafeDescription(String value) {
    if (value==null) return false;
    String v = value.trim();
    return (v.length() >= 20)
        && (v.indexOf('\u0000') < 0)
        && (v.indexOf('<') < 0 && v.indexOf('>') < 0)
        && (!DANGEROUS_PROTOCOL_PREFIX.matcher(v).find());
  }
}
