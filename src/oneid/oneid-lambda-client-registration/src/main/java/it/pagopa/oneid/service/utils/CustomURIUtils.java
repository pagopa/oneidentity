package it.pagopa.oneid.service.utils;

import com.google.common.net.InetAddresses;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.exception.InvalidUriException;
import it.pagopa.oneid.model.enums.ClientRegistrationErrorCode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

@CustomLogging
public class CustomURIUtils {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

  private static final Pattern DANGEROUS_PATTERN = Pattern.compile("(?i)^(javascript|data|vbscript|file):.*");

  private static final Pattern DOMAIN_PATTERN =
      Pattern.compile(
          "^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)"
              + "(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))*"
              + "\\.[A-Za-z]{2,}$"
      );

  private static void checkDangerousSchema(String uriString) {
    // stop dangerous schema
    String trimmed = uriString.trim();
    if (DANGEROUS_PATTERN.matcher(trimmed).matches()) {
      Log.error("Dangerous schema");
      throw new InvalidUriException(ClientRegistrationErrorCode.INVALID_URI);
    }
  }

  public static void validateURI(String uriString) {
    if (StringUtils.isBlank(uriString)) {
      throw new InvalidUriException(ClientRegistrationErrorCode.EMPTY_URI);
    }
    try {
      checkDangerousSchema(uriString);
      URI uri = new URI(uriString);
      validateHttps(uri);
    } catch (URISyntaxException e) {
      throw new InvalidUriException(ClientRegistrationErrorCode.INVALID_URI);
    }
  }

  private static void validateHttps(URI uri) throws URISyntaxException {
    if (!"https".equalsIgnoreCase(uri.getScheme())) {
      Log.error("Scheme not valid: " + uri.getScheme());
      throw new URISyntaxException(uri.toString(), "Schema not valid");
    }
    String host = uri.getHost();
    // validate host
    if (host==null || host.isBlank()) {
      throw new URISyntaxException(uri.toString(), "Host not valid");
    }
    // validate userInfo
    if (uri.getUserInfo()!=null) {
      throw new URISyntaxException(uri.toString(), "User info not valid");
    }
    // Domain Check
    if (InetAddresses.isInetAddress(host) && !DOMAIN_PATTERN.matcher(host).matches()) {
      throw new URISyntaxException(uri.toString(), "Domain not valid");
    }
  }

  public static void validateHttpsOrMail(String uriString) {
    if (StringUtils.isBlank(uriString)) {
      throw new InvalidUriException(ClientRegistrationErrorCode.EMPTY_URI);
    }

    String trimmed = uriString.trim();
    checkDangerousSchema(trimmed);

    if (EMAIL_PATTERN.matcher(trimmed).matches()) {
      return; // Valid plain email
    }

    try {
      URI uri = new URI(trimmed);
      String scheme = uri.getScheme();

      if ("https".equalsIgnoreCase(scheme)) {
        validateHttps(uri);
      } else {
        throw new InvalidUriException(ClientRegistrationErrorCode.INVALID_URI);
      }
    } catch (URISyntaxException e) {
      throw new InvalidUriException(ClientRegistrationErrorCode.INVALID_URI);
    }
  }

  public static void validateEmail(String email) {
    if (StringUtils.isBlank(email)) {
      throw new InvalidUriException(ClientRegistrationErrorCode.EMPTY_URI);
    }

    String v = email.trim();

    if (DANGEROUS_PATTERN.matcher(v).matches()) {
      throw new InvalidUriException(ClientRegistrationErrorCode.INVALID_URI);
    }

    if (!EMAIL_PATTERN.matcher(v).matches()) {
      throw new InvalidUriException(ClientRegistrationErrorCode.INVALID_URI);
    }
  }
}