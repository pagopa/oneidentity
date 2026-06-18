package it.pagopa.oneid.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import java.util.Arrays;

public enum ClientSamlBinding {
  HTTP_POST("HTTP-POST", SamlBinding.HTTP_POST),
  HTTP_REDIRECT("HTTP-Redirect", SamlBinding.HTTP_REDIRECT);

  private final String value;
  private final SamlBinding samlBinding;

  ClientSamlBinding(String value, SamlBinding samlBinding) {
    this.value = value;
    this.samlBinding = samlBinding;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public SamlBinding toSamlBinding() {
    return samlBinding;
  }

  @JsonCreator
  public static ClientSamlBinding fromValue(String input) {
    if (input == null) {
      return null;
    }

    return Arrays.stream(values())
        .filter(binding -> binding.value.equalsIgnoreCase(input))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid samlBinding value: " + input));
  }

  public static ClientSamlBinding fromSamlBinding(SamlBinding samlBinding) {
    if (samlBinding == null || samlBinding == SamlBinding.HTTP_POST) {
      return HTTP_POST;
    }
    return HTTP_REDIRECT;
  }
}