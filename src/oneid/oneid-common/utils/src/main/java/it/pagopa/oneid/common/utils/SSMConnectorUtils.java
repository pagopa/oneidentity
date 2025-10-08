package it.pagopa.oneid.common.utils;

import java.util.Optional;

public interface SSMConnectorUtils {

  Optional<String> getParameter(String parameterName);

  boolean putSecureString(String parameterName, String value);

  boolean upsertSecureStringIfPresentOnlyIfChanged(String name, String value);

  boolean deleteParameter(String name);
}
