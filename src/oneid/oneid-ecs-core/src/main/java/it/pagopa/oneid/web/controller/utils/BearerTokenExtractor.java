package it.pagopa.oneid.web.controller.utils;

import it.pagopa.oneid.exception.InvalidRequestMalformedHeaderAuthorizationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

public final class BearerTokenExtractor {

  private static final String BEARER_PREFIX = "Bearer ";

  private BearerTokenExtractor() {
  }

  public static String extract(String authorization) {
    if (StringUtils.isBlank(authorization) || !Strings.CI.startsWith(authorization,
        BEARER_PREFIX)) {
      throw new InvalidRequestMalformedHeaderAuthorizationException();
    }

    String accessToken = authorization.substring(BEARER_PREFIX.length()).trim();
    if (StringUtils.isBlank(accessToken)) {
      throw new InvalidRequestMalformedHeaderAuthorizationException();
    }

    return accessToken;
  }
}
