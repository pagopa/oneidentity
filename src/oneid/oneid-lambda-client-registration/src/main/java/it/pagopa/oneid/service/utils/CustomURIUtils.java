package it.pagopa.oneid.service.utils;

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

  public static void validateURI(String uriString) {
    if (StringUtils.isBlank(uriString)) {
      throw new InvalidUriException(ClientRegistrationErrorCode.REDIRECT_URI_NULL);
    }
    try {
      URI uri = new URI(uriString);
      // Scheme check
      if (!isValidScheme(uri.getScheme())) {
        Log.error("Scheme not valid: " + uri.getScheme());
        throw new URISyntaxException(uriString, "Schema not valid");
      }
      // Domain check
      if (!isValidDomain(uri.getHost())) {
        Log.error("Domain not valid: " + uri.getHost());
        throw new URISyntaxException(uriString, "Domain not valid");
      }
    } catch (URISyntaxException e) {
      throw new InvalidUriException(ClientRegistrationErrorCode.INVALID_REDIRECT_URI);
    }
  }

  private static boolean isValidScheme(String scheme) {
    String[] validSchemes = {"http", "https"};
    for (String validScheme : validSchemes) {
      if (validScheme.equalsIgnoreCase(scheme)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isValidDomain(String domain) {
    String domainPattern = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    return domain != null && Pattern.matches(domainPattern, domain);
  }
}