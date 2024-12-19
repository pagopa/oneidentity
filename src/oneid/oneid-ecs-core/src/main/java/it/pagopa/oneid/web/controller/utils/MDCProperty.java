package it.pagopa.oneid.web.controller.utils;

import lombok.Getter;

@Getter
public enum MDCProperty {
  CLIENT_ID("client.id"),
  CLIENT_STATE("client.state");

  private final String value;

  MDCProperty(String value) {
    this.value = value;
  }

}
