package it.pagopa.oneid.common.model.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public enum AuthLevel {

  L1("https://www.spid.gov.it/SpidL1"),
  L2("https://www.spid.gov.it/SpidL2"),
  L3("https://www.spid.gov.it/SpidL3"),
  ;

  private static final Map<String, AuthLevel> BY_VALUE = new HashMap<>();

  static {
    for (AuthLevel al : values()) {
      BY_VALUE.put(al.value, al);
    }
  }

  private final String value;

  AuthLevel(String value) {
    this.value = value;
  }

  public static AuthLevel authLevelFromValue(String value) {
    return BY_VALUE.get(value);
  }
  
}
