package it.pagopa.oneid.common.model.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public enum SamlBinding {

  HTTP_POST("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"),
  HTTP_REDIRECT("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");

  private static final Map<String, SamlBinding> BY_VALUE = new HashMap<>();

  static {
    for (SamlBinding bindingType : values()) {
      BY_VALUE.put(bindingType.value, bindingType);
    }
  }

  private final String value;

  SamlBinding(String value) {
    this.value = value;
  }

  public static SamlBinding samlBindingTypeFromValue(String value) {
    return BY_VALUE.getOrDefault(value, HTTP_POST);
  }

}
