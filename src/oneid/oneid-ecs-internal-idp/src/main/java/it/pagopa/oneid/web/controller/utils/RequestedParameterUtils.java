package it.pagopa.oneid.web.controller.utils;

import it.pagopa.oneid.common.model.enums.Identifier;
import java.util.Set;
import java.util.stream.Collectors;

public class RequestedParameterUtils {

  public static Set<String> mapToFriendlyNames(Set<String> requestedParameters) {
    return requestedParameters.stream()
        .map(key -> {
          try {
            return Identifier.valueOf(key).getFriendlyName();
          } catch (IllegalArgumentException e) {
            return key; // fallback
          }
        }).collect(Collectors.toSet());
  }
}
