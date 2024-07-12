package it.pagopa.oneid.common.model.enums;

import lombok.Getter;

@Getter
public enum AuthLevel {

  L1("https://www.spid.gov.it/SpidL1"),
  L2("https://www.spid.gov.it/SpidL2"),
  L3("https://www.spid.gov.it/SpidL3"),
  ;

  private final String value;

  AuthLevel(String value) {
    this.value = value;
  }

}
